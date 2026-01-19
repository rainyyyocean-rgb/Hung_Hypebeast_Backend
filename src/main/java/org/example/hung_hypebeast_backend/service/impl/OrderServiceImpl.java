package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.CartItemRequest;
import org.example.hung_hypebeast_backend.dto.request.OrderRequest;
import org.example.hung_hypebeast_backend.dto.response.OrderResponse;
import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.entity.OrderItem;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.example.hung_hypebeast_backend.exception.InsufficientStockException;
import org.example.hung_hypebeast_backend.exception.InvalidOrderStatusException;
import org.example.hung_hypebeast_backend.exception.OrderNotFoundException;
import org.example.hung_hypebeast_backend.exception.ProductNotFoundException;
import org.example.hung_hypebeast_backend.mapper.OrderMapper;
import org.example.hung_hypebeast_backend.repository.OrderItemRepository;
import org.example.hung_hypebeast_backend.repository.OrderRepository;
import org.example.hung_hypebeast_backend.repository.ProductSkuRepository;
import org.example.hung_hypebeast_backend.service.EmailService;
import org.example.hung_hypebeast_backend.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ProductSkuRepository productSkuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Sử dụng mapper để tạo Order entity từ request
        Order order = orderMapper.toEntity(request);
        boolean isCOD = "COD".equalsIgnoreCase(request.getPaymentMethod());

        // 2. Lưu order với trạng thái khởi tạo
        order = orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 3. Xử lý từng item: trừ kho và tạo OrderItem
        for (CartItemRequest itemReq : request.getItems()) {

            // Lock DB để tránh race condition
            ProductSku sku = productSkuRepository.findByIdWithLock(itemReq.getSkuId())
                    .orElseThrow(() -> new ProductNotFoundException("Sản phẩm không tồn tại"));

            // Validate số lượng tồn kho
            if (sku.getQuantity() < itemReq.getQuantity()) {
                throw new InsufficientStockException(
                    sku.getProduct().getName(),
                    sku.getQuantity(),
                    itemReq.getQuantity()
                );
            }

            // Trừ kho
            sku.setQuantity(sku.getQuantity() - itemReq.getQuantity());
            productSkuRepository.save(sku);

            // Tạo Order Item
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setSkuId(sku.getId());
            orderItem.setProductName(sku.getProduct().getName());
            orderItem.setSize(sku.getSize());
            orderItem.setColor(sku.getColor());
            orderItem.setQuantity(itemReq.getQuantity());

            BigDecimal price = sku.getPrice() != null ? sku.getPrice() : sku.getProduct().getBasePrice();
            orderItem.setPrice(price);

            totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
            orderItems.add(orderItem);
        }

        // 4. Lưu items và cập nhật tổng tiền
        orderItemRepository.saveAll(orderItems);
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems);
        orderRepository.save(order);

        // 5. Gửi email cho đơn COD ngay sau khi đặt hàng thành công
        if (isCOD) {
            emailService.sendOrderConfirmationEmail(order);
        }

        // 6. Sử dụng mapper để tạo response
        return orderMapper.toCreateOrderResponse(order, isCOD);
    }

    @Override
    @Transactional
    public void cancelUnpaidOrders() {
        // Tìm các đơn PENDING và đã hết hạn
        List<Order> expiredOrders = orderRepository.findAllByStatusAndExpiredAtBefore(
                OrderStatus.PENDING, LocalDateTime.now()
        ).stream().toList();

        for (Order order : expiredOrders) {
            System.out.println("Đang hủy đơn hàng ID: " + order.getId());

            // 1. Đổi trạng thái sang CANCELED
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);

            // 2. Hoàn lại kho
            restockOrder(order);
        }
    }

    @Override
    public Page<OrderResponse> getOrdersForAdmin(OrderStatus status, String phone, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findOrdersForAdmin(status, phone, pageable);

        // Sử dụng mapper để chuyển đổi
        return orderPage.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // 2. Lấy trạng thái cũ
        OrderStatus oldStatus = order.getStatus();

        // 3. XỬ LÝ LOGIC ĐẶC BIỆT

        // CASE A: Admin hủy đơn -> Phải hoàn kho
        if (newStatus == OrderStatus.CANCELED && oldStatus != OrderStatus.CANCELED) {
            restockOrder(order);
            System.out.println("Admin đã hủy đơn " + orderId + " -> Đã hoàn kho.");
        }

        // CASE B: Không cho phép khôi phục đơn đã hủy
        if (oldStatus == OrderStatus.CANCELED && newStatus != OrderStatus.CANCELED) {
            throw new InvalidOrderStatusException("Không thể khôi phục đơn hàng đã hủy! Hãy bảo khách đặt đơn mới.");
        }

        // CASE C: Xác nhận thanh toán (PENDING -> CONFIRMED)
        if (newStatus == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING) {
            order.setExpiredAt(null);
        }

        // 4. Lưu thay đổi
        order.setStatus(newStatus);
        orderRepository.save(order);

        // 5. Sử dụng mapper để tạo response
        return orderMapper.toUpdateStatusResponse(order);
    }

    /**
     * Hoàn lại kho cho các items trong đơn hàng
     */
    private void restockOrder(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductSku sku = productSkuRepository.findById(item.getSkuId()).orElse(null);
            if (sku != null) {
                sku.setQuantity(sku.getQuantity() + item.getQuantity());
                productSkuRepository.save(sku);
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse trackOrderByToken(String trackingToken) {
        // 1. Tìm đơn hàng theo tracking token
        Order order = orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new OrderNotFoundException("mã tracking", trackingToken));

        // 2. Force load items để tránh lazy loading
        order.getItems().size();

        // 3. Sử dụng mapper để tạo response
        return orderMapper.toTrackingResponse(order);
    }
}


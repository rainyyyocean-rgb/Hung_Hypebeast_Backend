package org.example.hung_hypebeast_backend.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.hung_hypebeast_backend.dto.request.CartItemRequest;
import org.example.hung_hypebeast_backend.dto.request.OrderRequest;
import org.example.hung_hypebeast_backend.dto.response.OrderResponse;
import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.entity.OrderItem;
import org.example.hung_hypebeast_backend.entity.ProductSku;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.example.hung_hypebeast_backend.enums.PaymentMethod;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final ProductSkuRepository productSkuRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final EmailService emailService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderResponse createOrder(OrderRequest request) {

        // 1. Khởi tạo đơn hàng cơ bản
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod())); // "COD" hoặc "SEPAY"
        order.setCreatedAt(LocalDateTime.now());
        order.setTrackingToken(UUID.randomUUID().toString());

        order.setTotalAmount(BigDecimal.ZERO);
        // 🔥 LOGIC RẼ NHÁNH THANH TOÁN
        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            // Trường hợp COD: Chốt đơn luôn
            order.setStatus(OrderStatus.CONFIRMED);
            order.setExpiredAt(null); // Không bao giờ hết hạn tự động
        } else {
            // Trường hợp Chuyển khoản (SePay): Chờ 15 phút
            order.setStatus(OrderStatus.PENDING);
            order.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        }

        order = orderRepository.save(order); // Lưu trạng thái khởi tạo

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // 2. VÒNG LẶP TRỪ KHO (Áp dụng cho cả COD và SEPAY)
        // Tại sao COD cũng phải trừ ngay?
        // Vì nếu không trừ, người khác vào mua mất cái áo đó thì sao?
        // Khác biệt là COD trừ xong thì giữ luôn, còn SePay trừ xong 15p sau có thể bị cộng lại.
        for (CartItemRequest itemReq : request.getItems()) {

            // Lock DB
            ProductSku sku = productSkuRepository.findByIdWithLock(itemReq.getSkuId())
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại"));

            if (sku.getQuantity() < itemReq.getQuantity()) {
                throw new RuntimeException("Sản phẩm " + sku.getProduct().getName() + " không đủ số lượng!");
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

        orderItemRepository.saveAll(orderItems);
        order.setTotalAmount(totalAmount);
        order.setItems(orderItems); // Set items để email service có thể truy cập
        orderRepository.save(order);

        // 3. Gửi email cho đơn COD ngay sau khi đặt hàng thành công
        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            emailService.sendOrderConfirmationEmail(order);
        }

        // 4. Trả về thông báo tùy theo phương thức
        String message = "COD".equalsIgnoreCase(request.getPaymentMethod())
                ? "Đặt hàng thành công! Chúng tôi sẽ sớm liên hệ."
                : "Vui lòng chuyển khoản trong vòng 15 phút để giữ hàng.";

        return OrderResponse.builder()
                .orderId(order.getId())
                .trackingToken(order.getTrackingToken())
                .status(order.getStatus().name())
                .totalAmount(totalAmount)
                .expiredAt(order.getExpiredAt())
                .message(message)
                .build();
    }

    @Override
    @Transactional
    public void cancelUnpaidOrders() {
        // Tìm các đơn PENDING và đã hết hạn (expiredAt < now)
        List<Order> expiredOrders = orderRepository.findAllByStatusAndExpiredAtBefore(
                OrderStatus.PENDING, LocalDateTime.now()
        ).stream().toList();

        for (Order order : expiredOrders) {
            System.out.println("Đang hủy đơn hàng ID: " + order.getId());

            // 1. Đổi trạng thái sang CANCELED
            order.setStatus(OrderStatus.CANCELED);
            orderRepository.save(order);

            // 2. Hoàn lại kho (Restock)
            for (OrderItem item : order.getItems()) {
                ProductSku sku = productSkuRepository.findById(item.getSkuId()).orElse(null);
                if (sku != null) {
                    // Cộng lại số lượng đã trừ
                    sku.setQuantity(sku.getQuantity() + item.getQuantity());
                    productSkuRepository.save(sku);
                    System.out.println("-> Đã hoàn lại " + item.getQuantity() + " cái cho SKU " + sku.getSkuCode());
                }
            }
        }
    }

    @Override
    public Page<OrderResponse> getOrdersForAdmin(OrderStatus status, String phone, Pageable pageable) {
        Page<Order> orderPage = orderRepository.findOrdersForAdmin(status, phone, pageable);

        return orderPage.map(order -> OrderResponse.builder()
                .orderId(order.getId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .paymentMethod(String.valueOf(order.getPaymentMethod()))
                .trackingToken(order.getTrackingToken())
                .createdAt(order.getCreatedAt())
                .build());
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng ID: " + orderId));

        // 2. Validate trạng thái gửi lên (Có đúng chính tả enum không)

        // Kiểm tra trạng thái cũ
        OrderStatus oldStatus = order.getStatus();

        // 3. XỬ LÝ LOGIC ĐẶC BIỆT

        // CASE A: Nếu Admin muốn HỦY đơn -> Phải hoàn kho (Restock)
        // Chỉ hoàn kho nếu đơn cũ CHƯA hủy (để tránh cộng dồn nhiều lần)
        if (newStatus == OrderStatus.CANCELED && oldStatus != OrderStatus.CANCELED) {
            for (OrderItem item : order.getItems()) {
                ProductSku sku = productSkuRepository.findById(item.getSkuId()).orElse(null);
                if (sku != null) {
                    sku.setQuantity(sku.getQuantity() + item.getQuantity());
                    productSkuRepository.save(sku);
                }
            }
            System.out.println("Admin đã hủy đơn " + orderId + " -> Đã hoàn kho.");
        }

        // CASE B: Nếu Admin muốn khôi phục lại đơn đã hủy (CANCELLED -> CONFIRMED/PENDING)
        // (Đây là ca khó, thường ít làm vì phải check kho lại. Ở đây mình tạm chặn cho đơn giản)
        if (oldStatus == OrderStatus.CANCELED && newStatus != OrderStatus.CANCELED) {
            throw new RuntimeException("Không thể khôi phục đơn hàng đã hủy! Hãy bảo khách đặt đơn mới.");
        }

        // CASE C: Nếu Admin xác nhận thanh toán (PENDING -> CONFIRMED)
        // Phải set expiredAt = null để Cronjob không tự động hủy đơn này nữa
        if (newStatus == OrderStatus.CONFIRMED && oldStatus == OrderStatus.PENDING) {
            order.setExpiredAt(null);
        }

        // 4. Lưu thay đổi
        order.setStatus(newStatus);
        orderRepository.save(order);

        // 5. Trả về kết quả
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerName(order.getCustomerName())
                .status(order.getStatus().name())
                .message("Cập nhật trạng thái thành công!")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse trackOrderByToken(String trackingToken) {
        // 1. Tìm đơn hàng theo tracking token
        Order order = orderRepository.findByTrackingToken(trackingToken)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã tracking: " + trackingToken));

        // 2. Force load items để tránh lazy loading
        order.getItems().size();

        // 3. Trả về thông tin đơn hàng
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerName(order.getCustomerName())
                .customerPhone(order.getCustomerPhone())
                .customerEmail(order.getCustomerEmail())
                .shippingAddress(order.getShippingAddress())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus().name())
                .paymentMethod(order.getPaymentMethod().name())
                .trackingToken(order.getTrackingToken())
                .createdAt(order.getCreatedAt())
                .message("Trạng thái đơn hàng: " + getStatusMessage(order.getStatus()))
                .build();
    }

    private String getStatusMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ thanh toán";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao hàng";
            case COMPLETED -> "Đã hoàn thành";
            case CANCELED -> "Đã hủy";
        };
    }
}


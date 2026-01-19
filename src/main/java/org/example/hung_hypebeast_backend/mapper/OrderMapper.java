package org.example.hung_hypebeast_backend.mapper;

import org.example.hung_hypebeast_backend.dto.request.OrderRequest;
import org.example.hung_hypebeast_backend.dto.response.OrderResponse;
import org.example.hung_hypebeast_backend.entity.Order;
import org.example.hung_hypebeast_backend.enums.OrderStatus;
import org.example.hung_hypebeast_backend.enums.PaymentMethod;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Mapper để chuyển đổi giữa Order entity và các DTOs
 */
@Component
public class OrderMapper {

    /**
     * Tạo Order entity từ OrderRequest
     *
     * @param request OrderRequest từ client
     * @return Order entity mới (chưa có items và totalAmount)
     */
    public Order toEntity(OrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        order.setCreatedAt(LocalDateTime.now());
        order.setTrackingToken(UUID.randomUUID().toString());
        order.setTotalAmount(BigDecimal.ZERO);

        // Logic rẽ nhánh thanh toán
        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setExpiredAt(null);
        } else {
            order.setStatus(OrderStatus.PENDING);
            order.setExpiredAt(LocalDateTime.now().plusMinutes(15));
        }

        return order;
    }

    /**
     * Chuyển đổi Order entity sang OrderResponse sau khi tạo đơn
     *
     * @param order Order entity
     * @param isCOD Có phải phương thức COD không
     * @return OrderResponse với message phù hợp
     */
    public OrderResponse toCreateOrderResponse(Order order, boolean isCOD) {
        String message = isCOD
                ? "Đặt hàng thành công! Chúng tôi sẽ sớm liên hệ."
                : "Vui lòng chuyển khoản trong vòng 15 phút để giữ hàng.";

        return OrderResponse.builder()
                .orderId(order.getId())
                .trackingToken(order.getTrackingToken())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .expiredAt(order.getExpiredAt())
                .message(message)
                .build();
    }

    /**
     * Chuyển đổi Order entity sang OrderResponse (dùng cho Admin)
     *
     * @param order Order entity
     * @return OrderResponse với đầy đủ thông tin
     */
    public OrderResponse toOrderResponse(Order order) {
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
                .build();
    }

    /**
     * Tạo OrderResponse sau khi cập nhật trạng thái
     *
     * @param order Order entity sau khi cập nhật
     * @return OrderResponse với message thành công
     */
    public OrderResponse toUpdateStatusResponse(Order order) {
        return OrderResponse.builder()
                .orderId(order.getId())
                .customerName(order.getCustomerName())
                .status(order.getStatus().name())
                .message("Cập nhật trạng thái thành công!")
                .build();
    }

    /**
     * Tạo OrderResponse cho tracking với message trạng thái
     *
     * @param order Order entity
     * @return OrderResponse với thông tin tracking
     */
    public OrderResponse toTrackingResponse(Order order) {
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

    /**
     * Lấy message mô tả trạng thái đơn hàng
     *
     * @param status OrderStatus enum
     * @return Message tiếng Việt
     */
    public String getStatusMessage(OrderStatus status) {
        return switch (status) {
            case PENDING -> "Chờ thanh toán";
            case CONFIRMED -> "Đã xác nhận";
            case SHIPPING -> "Đang giao hàng";
            case COMPLETED -> "Đã hoàn thành";
            case CANCELED -> "Đã hủy";
        };
    }
}


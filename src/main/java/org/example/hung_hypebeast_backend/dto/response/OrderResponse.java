package org.example.hung_hypebeast_backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private Long orderId;
    private String customerName; // Admin cần biết tên khách
    private String customerPhone; // Để gọi điện chốt đơn
    private String shippingAddress;
    private String customerEmail;
    private String status;
    private BigDecimal totalAmount;
    private String trackingToken;
    private String paymentMethod; // COD hay SEPAY
    private LocalDateTime createdAt;
    private LocalDateTime expiredAt;
    private String message;
}

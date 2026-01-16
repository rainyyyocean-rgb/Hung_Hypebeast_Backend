package org.example.hung_hypebeast_backend.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    // Thông tin khách hàng (Guest)
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;

    // Phương thức thanh toán: "COD" hoặc "SEPAY"
    private String paymentMethod;

    // Danh sách hàng mua
    private List<CartItemRequest> items; // Tái sử dụng class CartItemRequest đã tạo ở phần trước
}

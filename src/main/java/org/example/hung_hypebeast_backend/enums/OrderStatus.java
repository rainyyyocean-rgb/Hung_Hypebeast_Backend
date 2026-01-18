package org.example.hung_hypebeast_backend.enums;

public enum OrderStatus {
    PENDING,      // Chờ thanh toán (chỉ dùng cho SePay)
    CONFIRMED,    // Đã xác nhận (dùng cho cả COD và SePay sau khi thanh toán)
    SHIPPING,     // Đang giao hàng
    COMPLETED,    // Đã hoàn thành
    CANCELED      // Đã hủy
}

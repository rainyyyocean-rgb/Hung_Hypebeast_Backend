package org.example.hung_hypebeast_backend.exception;

import org.example.hung_hypebeast_backend.enums.OrderStatus;

public class InvalidOrderStatusException extends RuntimeException {
    public InvalidOrderStatusException(String message) {
        super(message);
    }

    public InvalidOrderStatusException(OrderStatus currentStatus, OrderStatus expectedStatus) {
        super(String.format("Trạng thái đơn hàng không hợp lệ. Hiện tại: %s, Yêu cầu: %s",
            currentStatus, expectedStatus));
    }

    public InvalidOrderStatusException(OrderStatus currentStatus, String operation) {
        super(String.format("Không thể %s đơn hàng ở trạng thái %s", operation, currentStatus));
    }
}


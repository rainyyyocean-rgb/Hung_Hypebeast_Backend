package org.example.hung_hypebeast_backend.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(Long orderId) {
        super("Không tìm thấy đơn hàng với ID: " + orderId);
    }

    public OrderNotFoundException(String field, String value) {
        super(String.format("Không tìm thấy đơn hàng với %s: %s", field, value));
    }
}

package org.example.hung_hypebeast_backend.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(Long productId) {
        super("Sản phẩm không tồn tại với ID: " + productId);
    }
}


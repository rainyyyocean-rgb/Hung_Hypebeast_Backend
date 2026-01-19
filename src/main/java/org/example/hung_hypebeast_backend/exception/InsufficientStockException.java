package org.example.hung_hypebeast_backend.exception;

public class InsufficientStockException extends RuntimeException {
    private final String productName;
    private final int available;
    private final int requested;

    public InsufficientStockException(String productName, int available, int requested) {
        super(String.format("Sản phẩm '%s' không đủ số lượng! Còn lại: %d, yêu cầu: %d",
            productName, available, requested));
        this.productName = productName;
        this.available = available;
        this.requested = requested;
    }

    public InsufficientStockException(String message) {
        super(message);
        this.productName = null;
        this.available = 0;
        this.requested = 0;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailable() {
        return available;
    }

    public int getRequested() {
        return requested;
    }
}


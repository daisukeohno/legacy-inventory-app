package com.example.inventory.exception;

public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final String productName;

    public InsufficientStockException(Long productId, String productName, int requested) {
        super("Insufficient stock for product '" + productName + "' (id=" + productId
                + ", requested=" + requested + ")");
        this.productId = productId;
        this.productName = productName;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }
}

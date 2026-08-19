package com.example.inventory.service;

/** 在庫不足。旧実装ではエラー画面へのforwardで表現していた。 */
public class InsufficientStockException extends RuntimeException {

    private final Long productId;
    private final String productName;
    private final int availableQuantity;
    private final int requestedQuantity;

    public InsufficientStockException(Long productId, String productName,
                                      int availableQuantity, int requestedQuantity) {
        super("「" + productName + "」の在庫が不足しています(在庫数: " + availableQuantity + ")。");
        this.productId = productId;
        this.productName = productName;
        this.availableQuantity = availableQuantity;
        this.requestedQuantity = requestedQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}

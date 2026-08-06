package com.example.inventory.service;

/** 在庫不足。旧 OrderSaveAction のエラー表示に相当し、REST では 409 を返す。 */
public class InsufficientStockException extends RuntimeException {

    private final String productName;
    private final int availableQuantity;

    public InsufficientStockException(String productName, int availableQuantity) {
        super("「" + productName + "」の在庫が不足しています(在庫数: " + availableQuantity + ")。");
        this.productName = productName;
        this.availableQuantity = availableQuantity;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }
}

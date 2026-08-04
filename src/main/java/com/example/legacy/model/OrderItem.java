package com.example.legacy.model;

import java.io.Serializable;

public class OrderItem implements Serializable {

    private int productId;
    private String productName;
    private double unitPrice;
    private int quantity;

    public OrderItem() {
    }

    public OrderItem(int productId, String productName, double unitPrice, int quantity) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // 小計計算がモデルにもJSPにも散らばりがちなのがレガシーコードの典型例
    public double getSubtotal() {
        return unitPrice * quantity;
    }
}

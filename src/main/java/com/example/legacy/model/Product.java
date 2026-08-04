package com.example.legacy.model;

import java.io.Serializable;

/**
 * 商品(在庫)モデル。
 * ※レガシー実装のため、金額はdouble、DTO/Entityの区別なくそのままJSPまで持ち回している。
 */
public class Product implements Serializable {

    private int id;
    private String sku;
    private String name;
    private double price;
    private int stockQuantity;

    public Product() {
    }

    public Product(int id, String sku, String name, double price, int stockQuantity) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public boolean isLowStock() {
        // マジックナンバー直書き。しきい値を外部化していないのもレガシーな点。
        return stockQuantity < 10;
    }
}

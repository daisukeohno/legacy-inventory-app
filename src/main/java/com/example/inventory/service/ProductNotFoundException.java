package com.example.inventory.service;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(int id) {
        super("商品が見つかりません(id: " + id + ")。");
    }
}

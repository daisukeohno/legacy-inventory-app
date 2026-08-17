package com.example.inventory.service;

public class ProductNotFoundException extends RuntimeException {

    public ProductNotFoundException(Long id) {
        super("商品が見つかりません(id: " + id + ")。");
    }
}

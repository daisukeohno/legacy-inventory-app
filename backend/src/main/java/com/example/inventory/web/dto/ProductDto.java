package com.example.inventory.web.dto;

import com.example.inventory.domain.Product;
import java.math.BigDecimal;

public record ProductDto(Long id, String sku, String name, BigDecimal price, int stockQuantity,
                         boolean lowStock) {

    public static ProductDto from(Product product, int lowStockThreshold) {
        return new ProductDto(product.getId(), product.getSku(), product.getName(), product.getPrice(),
                product.getStockQuantity(), product.isLowStock(lowStockThreshold));
    }
}

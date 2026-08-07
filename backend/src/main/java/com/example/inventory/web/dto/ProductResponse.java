package com.example.inventory.web.dto;

import com.example.inventory.domain.Product;
import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        BigDecimal price,
        int stockQuantity,
        boolean lowStock) {

    public static ProductResponse of(Product product, boolean lowStock) {
        return new ProductResponse(product.getId(), product.getSku(), product.getName(),
                product.getPrice(), product.getStockQuantity(), lowStock);
    }
}

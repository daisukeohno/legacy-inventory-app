package com.example.inventory.dto;

import com.example.inventory.entity.Product;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductDto(
        Integer id,

        @NotBlank(message = "SKUを入力してください。")
        String sku,

        @NotBlank(message = "商品名を入力してください。")
        String name,

        @NotNull(message = "単価を入力してください。")
        @DecimalMin(value = "0", message = "単価は0以上で入力してください。")
        BigDecimal price,

        @NotNull(message = "在庫数を入力してください。")
        @Min(value = 0, message = "在庫数は0以上で入力してください。")
        Integer stockQuantity,

        boolean lowStock) {

    public static ProductDto from(Product product) {
        return new ProductDto(product.getId(), product.getSku(), product.getName(),
                product.getPrice(), product.getStockQuantity(), product.isLowStock());
    }
}

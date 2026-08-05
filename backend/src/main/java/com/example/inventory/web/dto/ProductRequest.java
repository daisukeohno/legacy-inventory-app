package com.example.inventory.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ProductRequest(
        @NotBlank(message = "SKUを入力してください。") String sku,
        @NotBlank(message = "商品名を入力してください。") String name,
        @NotNull(message = "価格を入力してください。")
        @DecimalMin(value = "0", message = "価格は0以上で入力してください。")
        @Digits(integer = 19, fraction = 0, message = "価格は円単位の整数で入力してください。")
        BigDecimal price,
        @NotNull(message = "在庫数を入力してください。")
        @Min(value = 0, message = "在庫数は0以上で入力してください。")
        Integer stockQuantity) {
}

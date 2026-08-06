package com.example.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** 旧 ProductForm.validate() の検証内容を Bean Validation で再現。 */
public record ProductRequest(
        @NotBlank(message = "SKUを入力してください。")
        @Size(max = 50)
        String sku,

        @NotBlank(message = "商品名を入力してください。")
        @Size(max = 200)
        String name,

        @NotNull(message = "単価を入力してください。")
        @PositiveOrZero(message = "単価は0以上で入力してください。")
        BigDecimal price,

        @NotNull(message = "在庫数を入力してください。")
        @PositiveOrZero(message = "在庫数は0以上で入力してください。")
        Integer stockQuantity) {
}

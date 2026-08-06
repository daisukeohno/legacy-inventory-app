package com.example.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderItemRequest(
        @NotNull(message = "商品を指定してください。")
        Integer productId,

        @PositiveOrZero(message = "数量は0以上で入力してください。")
        Integer quantity) {
}

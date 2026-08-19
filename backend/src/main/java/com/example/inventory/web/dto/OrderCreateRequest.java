package com.example.inventory.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 旧 OrderForm（productIds[]/quantities[] の並行配列）を構造化したリクエスト。
 * 数量0以下や存在しない商品IDの行は旧 OrderSaveAction と同様に無視される。
 */
public record OrderCreateRequest(
        @NotBlank(message = "得意先名を入力してください。")
        String customerName,

        @Valid
        List<Item> items
) {

    public record Item(
            @NotNull(message = "商品IDを指定してください。")
            Long productId,
            Integer quantity
    ) {
    }

    public List<Item> items() {
        return items == null ? List.of() : items;
    }
}

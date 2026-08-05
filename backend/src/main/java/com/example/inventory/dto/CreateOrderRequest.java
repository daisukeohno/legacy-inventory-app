package com.example.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "得意先名を入力してください。")
        String customerName,

        @Valid
        List<OrderItemInput> items) {

    public List<OrderItemInput> items() {
        return items == null ? List.of() : items;
    }

    public record OrderItemInput(Integer productId, Integer quantity) {
    }
}

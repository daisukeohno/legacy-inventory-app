package com.example.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
        @NotBlank(message = "得意先名を入力してください。")
        String customerName,

        @NotNull
        @Valid
        List<OrderItemRequest> items) {
}

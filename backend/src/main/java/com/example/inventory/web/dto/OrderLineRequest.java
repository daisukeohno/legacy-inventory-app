package com.example.inventory.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderLineRequest(
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity) {
}

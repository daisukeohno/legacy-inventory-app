package com.example.inventory.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record OrderRequest(
        @NotNull Long customerId,
        @NotEmpty @Valid List<Item> items) {

    public record Item(
            @NotNull Long productId,
            @NotNull @PositiveOrZero Integer quantity) {
    }
}

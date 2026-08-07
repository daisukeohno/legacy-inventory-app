package com.example.inventory.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderRequest(
        @NotBlank String customerName,
        @NotEmpty @Valid List<OrderLineRequest> lines) {
}

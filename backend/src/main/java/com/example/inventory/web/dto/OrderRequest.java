package com.example.inventory.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record OrderRequest(
        @NotBlank(message = "得意先名を入力してください。") String customerName,
        @NotEmpty(message = "少なくとも1つの商品を数量1以上で選択してください。")
        @Valid List<Line> items) {

    public record Line(
            @NotNull(message = "商品を選択してください。") Long productId,
            @NotNull @Min(value = 1, message = "数量は1以上で入力してください。") Integer quantity) {
    }
}

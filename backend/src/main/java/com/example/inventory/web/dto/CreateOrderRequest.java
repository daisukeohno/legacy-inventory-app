package com.example.inventory.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "得意先名を入力してください。") String customerName,
        @NotEmpty(message = "少なくとも1つの商品を数量1以上で選択してください。")
        @Valid List<Line> items) {

    /** 旧 orderForm.jsp の productIds[]/quantities[] 並行配列を置き換える明細1件。 */
    public record Line(
            @NotNull(message = "商品を選択してください。") Long productId,
            @NotNull(message = "数量を入力してください。")
            @PositiveOrZero(message = "数量は0以上で入力してください。") Integer quantity) {
    }
}

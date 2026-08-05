package com.example.inventory.dto;

import com.example.inventory.entity.OrderItem;
import java.math.BigDecimal;

public record OrderItemDto(
        Integer productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal) {

    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(item.getProductId(), item.getProductName(),
                item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
    }
}

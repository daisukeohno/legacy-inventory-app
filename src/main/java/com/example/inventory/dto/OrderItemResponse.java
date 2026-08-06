package com.example.inventory.dto;

import com.example.inventory.domain.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(
        Integer id,
        Integer productId,
        String productName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal subtotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getId(), item.getProductId(), item.getProductName(),
                item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
    }
}

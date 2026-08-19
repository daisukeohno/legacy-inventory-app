package com.example.inventory.web.dto;

import com.example.inventory.domain.OrderItem;
import java.math.BigDecimal;

public record OrderItemDto(Long id, Long productId, String productName, BigDecimal unitPrice,
                           int quantity, BigDecimal subtotal) {

    public static OrderItemDto from(OrderItem item) {
        return new OrderItemDto(item.getId(), item.getProductId(), item.getProductName(),
                item.getUnitPrice(), item.getQuantity(), item.getSubtotal());
    }
}

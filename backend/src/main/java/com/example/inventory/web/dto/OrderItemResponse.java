package com.example.inventory.web.dto;

import com.example.inventory.domain.OrderItem;
import java.math.BigDecimal;

public record OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, int quantity,
                                BigDecimal subtotal) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProductId(), item.getProductName(), item.getUnitPrice(),
                item.getQuantity(), item.getSubtotal());
    }
}

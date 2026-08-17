package com.example.inventory.web.dto;

import com.example.inventory.domain.Order;

import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        Long id,
        String customerName,
        String orderDate,
        String status,
        List<OrderItemDto> items,
        BigDecimal totalAmount
) {

    public static OrderDto of(Order order) {
        return new OrderDto(
                order.getId(),
                order.getCustomerName(),
                order.getOrderDate(),
                order.getStatus(),
                order.getItems().stream().map(OrderItemDto::of).toList(),
                order.getTotalAmount()
        );
    }
}

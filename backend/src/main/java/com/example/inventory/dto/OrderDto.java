package com.example.inventory.dto;

import com.example.inventory.entity.Order;
import java.math.BigDecimal;
import java.util.List;

public record OrderDto(
        Integer id,
        String customerName,
        String orderDate,
        String status,
        List<OrderItemDto> items,
        BigDecimal totalAmount) {

    public static OrderDto from(Order order) {
        return new OrderDto(order.getId(), order.getCustomerName(), order.getOrderDate(),
                order.getStatus(),
                order.getItems().stream().map(OrderItemDto::from).toList(),
                order.getTotalAmount());
    }
}

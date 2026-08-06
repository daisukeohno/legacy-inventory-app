package com.example.inventory.dto;

import com.example.inventory.domain.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        Integer id,
        String customerName,
        LocalDate orderDate,
        String status,
        List<OrderItemResponse> items,
        BigDecimal totalAmount) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(order.getId(), order.getCustomerName(), order.getOrderDate(),
                order.getStatus(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getTotalAmount());
    }
}

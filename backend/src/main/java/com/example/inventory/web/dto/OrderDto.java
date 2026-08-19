package com.example.inventory.web.dto;

import com.example.inventory.domain.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderDto(Long id, String customerName, LocalDate orderDate, String status,
                       List<OrderItemDto> items, BigDecimal totalAmount) {

    public static OrderDto from(Order order) {
        return new OrderDto(order.getId(), order.getCustomerName(), order.getOrderDate(),
                order.getStatus(),
                order.getItems().stream().map(OrderItemDto::from).toList(),
                order.getTotalAmount());
    }
}

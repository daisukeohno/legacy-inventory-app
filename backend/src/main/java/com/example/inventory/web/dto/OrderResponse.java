package com.example.inventory.web.dto;

import com.example.inventory.domain.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(Long id, Long customerId, String customerName, LocalDate orderDate, String status,
                            List<OrderItemResponse> items, BigDecimal totalAmount) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getCustomer().getName(),
                order.getOrderDate(),
                order.getStatus().name(),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getTotalAmount());
    }
}

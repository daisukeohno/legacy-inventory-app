package com.example.inventory.web.dto;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        Long id,
        CustomerResponse customer,
        LocalDate orderDate,
        OrderStatus status,
        List<Item> items,
        BigDecimal totalAmount) {

    public record Item(Long productId, String productName, BigDecimal unitPrice, int quantity, BigDecimal subtotal) {
    }

    public static OrderResponse of(Order order) {
        List<Item> items = order.getItems().stream()
                .map(i -> new Item(i.getProduct().getId(), i.getProductName(), i.getUnitPrice(),
                        i.getQuantity(), i.getSubtotal()))
                .toList();
        return new OrderResponse(order.getId(), CustomerResponse.of(order.getCustomer()),
                order.getOrderDate(), order.getStatus(), items, order.getTotalAmount());
    }
}

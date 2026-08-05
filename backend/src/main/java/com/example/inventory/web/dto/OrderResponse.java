package com.example.inventory.web.dto;

import com.example.inventory.domain.Order;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrderResponse(Long id, Long customerId, String customerName, LocalDate orderDate,
                            String status, List<Item> items, BigDecimal totalAmount) {

    public record Item(Long productId, String productName, BigDecimal unitPrice, int quantity,
                       BigDecimal subtotal) {
    }

    public static OrderResponse of(Order order) {
        List<Item> items = order.getItems().stream()
                .map(i -> new Item(i.getProductId(), i.getProductName(), i.getUnitPrice(),
                        i.getQuantity(), i.getSubtotal()))
                .toList();
        return new OrderResponse(order.getId(), order.getCustomer().getId(), order.getCustomerName(),
                order.getOrderDate(), order.getStatus().name(), items, order.getTotalAmount());
    }
}

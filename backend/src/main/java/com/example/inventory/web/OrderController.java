package com.example.inventory.web;

import com.example.inventory.domain.Order;
import com.example.inventory.service.OrderService;
import com.example.inventory.web.dto.OrderRequest;
import com.example.inventory.web.dto.OrderResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.findAll().stream().map(OrderResponse::from).toList();
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        List<OrderService.OrderLine> lines = request.lines().stream()
                .map(l -> new OrderService.OrderLine(l.productId(), l.quantity()))
                .toList();
        Order order = orderService.placeOrder(request.customerName(), lines);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(order));
    }
}

package com.example.inventory.web;

import com.example.inventory.domain.Order;
import com.example.inventory.service.OrderService;
import com.example.inventory.web.dto.CreateOrderRequest;
import com.example.inventory.web.dto.OrderDto;
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
    public List<OrderDto> list() {
        return orderService.findAll().stream().map(OrderDto::from).toList();
    }

    @PostMapping
    public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
        List<OrderService.OrderLine> lines = request.items().stream()
                .map(line -> new OrderService.OrderLine(line.productId(), line.quantity()))
                .toList();
        Order created = orderService.createOrder(request.customerName(), lines);
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderDto.from(created));
    }
}

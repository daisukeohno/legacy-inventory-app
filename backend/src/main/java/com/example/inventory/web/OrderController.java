package com.example.inventory.web;

import com.example.inventory.service.OrderService;
import com.example.inventory.web.dto.OrderRequest;
import com.example.inventory.web.dto.OrderResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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
        return orderService.findAll().stream().map(OrderResponse::of).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@Valid @RequestBody OrderRequest request) {
        List<OrderService.OrderLine> lines = request.items().stream()
                .map(i -> new OrderService.OrderLine(i.productId(), i.quantity()))
                .toList();
        return OrderResponse.of(orderService.placeOrder(request.customerId(), lines));
    }
}

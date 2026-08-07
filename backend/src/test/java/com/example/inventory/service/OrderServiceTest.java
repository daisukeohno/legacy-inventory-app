package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.domain.Customer;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderStatus;
import com.example.inventory.domain.Product;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.support.H2IntegrationTest;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@H2IntegrationTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private OrderRepository orderRepository;

    private Long customerId;
    private Long laptopId;
    private Long mouseId;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();

        customerId = customerRepository.save(new Customer("株式会社サンプル商事", null)).getId();
        laptopId = productRepository.save(new Product("SKU-1001", "ノートPC 14インチ", new BigDecimal("128000"), 24)).getId();
        mouseId = productRepository.save(new Product("SKU-1002", "ワイヤレスマウス", new BigDecimal("2800"), 1)).getId();
    }

    @Test
    void placesOrderAndDecreasesStock() {
        Order order = orderService.placeOrder(customerId, List.of(
                new OrderService.OrderLine(laptopId, 2),
                new OrderService.OrderLine(mouseId, 1)));

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getOrderDate()).isEqualTo(LocalDate.now());
        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("258800"));
        assertThat(productRepository.findById(laptopId).orElseThrow().getStockQuantity()).isEqualTo(22);
        assertThat(productRepository.findById(mouseId).orElseThrow().getStockQuantity()).isZero();
    }

    @Test
    void rollsBackEntireOrderWhenAnyItemIsOutOfStock() {
        assertThatThrownBy(() -> orderService.placeOrder(customerId, List.of(
                new OrderService.OrderLine(laptopId, 2),
                new OrderService.OrderLine(mouseId, 5))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(productRepository.findById(laptopId).orElseThrow().getStockQuantity()).isEqualTo(24);
        assertThat(productRepository.findById(mouseId).orElseThrow().getStockQuantity()).isEqualTo(1);
        assertThat(orderRepository.count()).isZero();
    }

    @Test
    void skipsNonPositiveQuantities() {
        Order order = orderService.placeOrder(customerId, List.of(
                new OrderService.OrderLine(laptopId, 0),
                new OrderService.OrderLine(mouseId, 1)));

        assertThat(order.getItems()).hasSize(1);
        assertThat(productRepository.findById(laptopId).orElseThrow().getStockQuantity()).isEqualTo(24);
    }

    @Test
    void rejectsOrderWithoutAnyPositiveQuantity() {
        assertThatThrownBy(() -> orderService.placeOrder(customerId, List.of(
                new OrderService.OrderLine(laptopId, 0))))
                .isInstanceOf(InvalidOrderException.class);
    }

    @Test
    void rejectsUnknownCustomer() {
        assertThatThrownBy(() -> orderService.placeOrder(999_999L, List.of(
                new OrderService.OrderLine(laptopId, 1))))
                .isInstanceOf(NotFoundException.class);
    }
}

package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.Product;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.repository.CustomerRepository;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private Product laptop;
    private Product mouse;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        customerRepository.deleteAll();
        laptop = productRepository.save(new Product("SKU-1001", "ノートPC 14インチ", new BigDecimal("128000"), 24));
        mouse = productRepository.save(new Product("SKU-1002", "ワイヤレスマウス", new BigDecimal("2800"), 6));
    }

    @Test
    void placesOrderAndDecreasesStock() {
        Order order = orderService.placeOrder("株式会社サンプル商事", List.of(
                new OrderService.OrderLine(laptop.getId(), 2),
                new OrderService.OrderLine(mouse.getId(), 2)));

        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotalAmount()).isEqualByComparingTo("261600");
        assertThat(order.getTotalAmount().scale()).isZero();
        assertThat(productRepository.findById(laptop.getId()).orElseThrow().getStockQuantity()).isEqualTo(22);
        assertThat(productRepository.findById(mouse.getId()).orElseThrow().getStockQuantity()).isEqualTo(4);
    }

    @Test
    void reusesExistingCustomerByName() {
        orderService.placeOrder("株式会社サンプル商事", List.of(new OrderService.OrderLine(mouse.getId(), 1)));
        orderService.placeOrder("株式会社サンプル商事", List.of(new OrderService.OrderLine(mouse.getId(), 1)));

        assertThat(customerRepository.findAll()).hasSize(1);
    }

    @Test
    void rollsBackEverythingWhenAnyLineIsOutOfStock() {
        assertThatThrownBy(() -> orderService.placeOrder("株式会社サンプル商事", List.of(
                new OrderService.OrderLine(laptop.getId(), 2),
                new OrderService.OrderLine(mouse.getId(), 99))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(orderRepository.findAll()).isEmpty();
        assertThat(productRepository.findById(laptop.getId()).orElseThrow().getStockQuantity()).isEqualTo(24);
        assertThat(productRepository.findById(mouse.getId()).orElseThrow().getStockQuantity()).isEqualTo(6);
    }

    @Test
    void rejectsEmptyOrder() {
        assertThatThrownBy(() -> orderService.placeOrder("株式会社サンプル商事", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

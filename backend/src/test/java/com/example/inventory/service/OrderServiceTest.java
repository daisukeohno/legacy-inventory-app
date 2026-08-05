package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.AbstractPostgresTest;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderStatus;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class OrderServiceTest extends AbstractPostgresTest {

    @Autowired
    OrderService orderService;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    OrderRepository orderRepository;

    @Test
    void placeOrderDecreasesStockAndSavesOrderWithItems() {
        long productId = productId("SKU-1004");
        int before = stock(productId);
        long ordersBefore = orderRepository.count();

        Order order = orderService.placeOrder("株式会社サンプル商事",
                List.of(new OrderService.OrderLine(productId, 3)));

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo(OrderStatus.NEW);
        assertThat(order.getOrderDate()).isEqualTo(LocalDate.now());
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("47400");
        assertThat(order.getTotalAmount().scale()).isZero();
        assertThat(stock(productId)).isEqualTo(before - 3);
        assertThat(orderRepository.count()).isEqualTo(ordersBefore + 1);
    }

    @Test
    void placeOrderRollsBackEverythingWhenOneLineHasInsufficientStock() {
        long okId = productId("SKU-1001");
        long ngId = productId("SKU-1003");
        int okBefore = stock(okId);
        int ngBefore = stock(ngId);
        long ordersBefore = orderRepository.count();

        assertThatThrownBy(() -> orderService.placeOrder("新規得意先A", List.of(
                new OrderService.OrderLine(okId, 1),
                new OrderService.OrderLine(ngId, 999))))
                .isInstanceOf(InsufficientStockException.class);

        assertThat(stock(okId)).isEqualTo(okBefore);
        assertThat(stock(ngId)).isEqualTo(ngBefore);
        assertThat(orderRepository.count()).isEqualTo(ordersBefore);
    }

    @Test
    void placeOrderCreatesCustomerWhenNameIsUnknown() {
        Order order = orderService.placeOrder("新規得意先B",
                List.of(new OrderService.OrderLine(productId("SKU-1006"), 1)));

        assertThat(order.getCustomer().getId()).isNotNull();
        assertThat(order.getCustomerName()).isEqualTo("新規得意先B");
    }

    private long productId(String sku) {
        return productRepository.findAll().stream()
                .filter(p -> p.getSku().equals(sku)).findFirst().orElseThrow().getId();
    }

    private int stock(long id) {
        return productRepository.findById(id).orElseThrow().getStockQuantity();
    }
}

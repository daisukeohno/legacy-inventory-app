package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.dto.CreateOrderRequest;
import com.example.inventory.dto.CreateOrderRequest.OrderItemInput;
import com.example.inventory.dto.OrderDto;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class OrderServiceTest {

    private static final int LAPTOP_ID = 1;
    private static final int MOUSE_ID = 2;
    private static final int HUB_ID = 3;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void createsOrderAndDeductsStock() {
        int before = stockOf(LAPTOP_ID);

        OrderDto order = orderService.create(new CreateOrderRequest("株式会社テスト",
                List.of(new OrderItemInput(LAPTOP_ID, 2), new OrderItemInput(MOUSE_ID, 1))));

        assertThat(order.status()).isEqualTo("NEW");
        assertThat(order.items()).hasSize(2);
        assertThat(order.totalAmount()).isEqualByComparingTo(new BigDecimal("258800"));
        assertThat(order.items().get(0).subtotal()).isEqualByComparingTo(new BigDecimal("256000"));
        assertThat(stockOf(LAPTOP_ID)).isEqualTo(before - 2);
    }

    @Test
    void skipsItemsWithMissingOrNonPositiveQuantity() {
        int mouseBefore = stockOf(MOUSE_ID);

        OrderDto order = orderService.create(new CreateOrderRequest("株式会社テスト",
                List.of(new OrderItemInput(LAPTOP_ID, 1),
                        new OrderItemInput(MOUSE_ID, 0),
                        new OrderItemInput(MOUSE_ID, null),
                        new OrderItemInput(MOUSE_ID, -3))));

        assertThat(order.items()).hasSize(1);
        assertThat(stockOf(MOUSE_ID)).isEqualTo(mouseBefore);
    }

    @Test
    void rejectsOrderWithoutAnyValidItem() {
        long orderCountBefore = orderRepository.count();

        assertThatThrownBy(() -> orderService.create(new CreateOrderRequest("株式会社テスト",
                List.of(new OrderItemInput(LAPTOP_ID, 0)))))
                .isInstanceOf(InvalidOrderException.class);

        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
    }

    @Test
    void rollsBackStockAndOrderWhenStockIsInsufficient() {
        int laptopBefore = stockOf(LAPTOP_ID);
        int hubBefore = stockOf(HUB_ID);
        long orderCountBefore = orderRepository.count();

        assertThatThrownBy(() -> orderService.create(new CreateOrderRequest("株式会社テスト",
                List.of(new OrderItemInput(LAPTOP_ID, 1),
                        new OrderItemInput(HUB_ID, hubBefore + 1)))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("在庫が不足しています");

        assertThat(stockOf(LAPTOP_ID)).isEqualTo(laptopBefore);
        assertThat(stockOf(HUB_ID)).isEqualTo(hubBefore);
        assertThat(orderRepository.count()).isEqualTo(orderCountBefore);
    }

    private int stockOf(int productId) {
        return productRepository.findById(productId).orElseThrow().getStockQuantity();
    }
}

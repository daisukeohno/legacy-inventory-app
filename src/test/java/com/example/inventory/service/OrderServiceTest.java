package com.example.inventory.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.dto.OrderItemRequest;
import com.example.inventory.dto.OrderRequest;
import com.example.inventory.repository.OrderRepository;
import com.example.inventory.repository.ProductRepository;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class OrderServiceTest {

    private static final int LAPTOP_ID = 1;      // SKU-1001 在庫 24
    private static final int MOUSE_ID = 2;       // SKU-1002 在庫 6
    private static final int USB_HUB_ID = 3;     // SKU-1003 在庫 3

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void createOrderDecreasesStockAndComputesTotal() {
        Order order = orderService.createOrder(new OrderRequest("株式会社サンプル商事", List.of(
                new OrderItemRequest(LAPTOP_ID, 2),
                new OrderItemRequest(MOUSE_ID, 1))));

        assertThat(order.getId()).isNotNull();
        assertThat(order.getStatus()).isEqualTo("NEW");
        assertThat(order.getOrderDate()).isEqualTo(LocalDate.now());
        assertThat(order.getItems()).extracting(OrderItem::getProductName)
                .containsExactly("ノートPC 14インチ", "ワイヤレスマウス");
        assertThat(order.getTotalAmount()).isEqualByComparingTo("258800.00");

        assertThat(productRepository.findById(LAPTOP_ID).orElseThrow().getStockQuantity()).isEqualTo(22);
        assertThat(productRepository.findById(MOUSE_ID).orElseThrow().getStockQuantity()).isEqualTo(5);
    }

    @Test
    void createOrderSkipsNonPositiveOrMissingQuantitiesAndUnknownProducts() {
        Order order = orderService.createOrder(new OrderRequest("得意先", List.of(
                new OrderItemRequest(LAPTOP_ID, 0),
                new OrderItemRequest(MOUSE_ID, null),
                new OrderItemRequest(9999, 5),
                new OrderItemRequest(USB_HUB_ID, 1))));

        assertThat(order.getItems()).extracting(OrderItem::getProductId).containsExactly(USB_HUB_ID);
        assertThat(productRepository.findById(LAPTOP_ID).orElseThrow().getStockQuantity()).isEqualTo(24);
        assertThat(productRepository.findById(MOUSE_ID).orElseThrow().getStockQuantity()).isEqualTo(6);
        assertThat(productRepository.findById(USB_HUB_ID).orElseThrow().getStockQuantity()).isEqualTo(2);
    }

    @Test
    void createOrderRejectsOrderWithoutValidItems() {
        assertThatThrownBy(() -> orderService.createOrder(new OrderRequest("得意先", List.of(
                new OrderItemRequest(LAPTOP_ID, 0)))))
                .isInstanceOf(EmptyOrderException.class);

        assertThat(orderRepository.findAllByOrderByIdDesc()).hasSize(1);
    }

    @Test
    void createOrderThrowsInsufficientStockWithAvailableQuantity() {
        assertThatThrownBy(() -> orderService.createOrder(new OrderRequest("得意先", List.of(
                new OrderItemRequest(USB_HUB_ID, 4)))))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("USB-Cハブ")
                .hasMessageContaining("在庫数: 3");
    }
}

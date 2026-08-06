package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void seedOrderIsLoadedWithItemsAndTotal() {
        List<Order> orders = orderRepository.findAllByOrderByIdDesc();

        assertThat(orders).hasSize(1);
        Order seeded = orders.get(0);
        assertThat(seeded.getCustomerName()).isEqualTo("株式会社サンプル商事");
        assertThat(seeded.getOrderDate()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(seeded.getStatus()).isEqualTo("SHIPPED");
        assertThat(seeded.getItems()).hasSize(2);
        assertThat(seeded.getTotalAmount()).isEqualByComparingTo("261600.00");
    }

    @Test
    void saveCascadesItemsAndListsNewestFirst() {
        Order order = new Order("合同会社デモロジスティクス", LocalDate.of(2026, 8, 1), "NEW");
        order.addItem(new OrderItem(1, "ノートPC 14インチ", new BigDecimal("128000.00"), 1));
        orderRepository.saveAndFlush(order);

        List<Order> orders = orderRepository.findAllByOrderByIdDesc();
        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getCustomerName()).isEqualTo("合同会社デモロジスティクス");
        assertThat(orders.get(0).getItems()).hasSize(1);
        assertThat(orders.get(0).getTotalAmount()).isEqualByComparingTo("128000.00");
    }
}

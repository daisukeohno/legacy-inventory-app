package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Order;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("シード注文は明細2件・合計261,600円")
    void seedOrder() {
        Order order = orderRepository.findAllByOrderByIdDesc().get(0);

        assertThat(order.getCustomerName()).isEqualTo("株式会社サンプル商事");
        assertThat(order.getOrderDate()).isEqualTo(LocalDate.of(2026, 7, 20));
        assertThat(order.getStatus()).isEqualTo("SHIPPED");
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getTotalAmount()).isEqualByComparingTo("261600.00");
    }
}

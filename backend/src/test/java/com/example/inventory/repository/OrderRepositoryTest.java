package com.example.inventory.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.inventory.domain.Customer;
import com.example.inventory.domain.Order;
import com.example.inventory.domain.OrderItem;
import com.example.inventory.domain.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    void cascadesItemsAndComputesTotal() {
        Customer customer = customerRepository.save(new Customer("株式会社サンプル商事", null));
        Order order = new Order(customer, LocalDate.of(2026, 7, 20), OrderStatus.NEW);
        order.addItem(new OrderItem(1L, "ノートPC 14インチ", new BigDecimal("128000"), 2));
        order.addItem(new OrderItem(2L, "ワイヤレスマウス", new BigDecimal("2800"), 2));
        orderRepository.saveAndFlush(order);

        Order loaded = orderRepository.findAllWithItems().get(0);
        assertThat(loaded.getItems()).hasSize(2);
        assertThat(loaded.getCustomer().getName()).isEqualTo("株式会社サンプル商事");
        assertThat(loaded.getTotalAmount()).isEqualByComparingTo("261600");
        assertThat(loaded.getTotalAmount().scale()).isZero();
    }
}

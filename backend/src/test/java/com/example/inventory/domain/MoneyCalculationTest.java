package com.example.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MoneyCalculationTest {

    @Test
    void subtotalIsScaleZeroHalfUp() {
        Product product = new Product("SKU-1", "テスト商品", new BigDecimal("1234.5"), 100);
        OrderItem item = new OrderItem(product, 3);

        assertThat(item.getUnitPrice()).isEqualByComparingTo("1235");
        assertThat(item.getSubtotal()).isEqualTo(new BigDecimal("3705"));
        assertThat(item.getSubtotal().scale()).isZero();
    }

    @Test
    void totalAmountSumsSubtotalsWithScaleZero() {
        Customer customer = new Customer("株式会社サンプル商事", null);
        Order order = new Order(customer, LocalDate.of(2026, 7, 20), OrderStatus.NEW);
        order.addItem(new OrderItem(new Product("SKU-1001", "ノートPC 14インチ", new BigDecimal("128000"), 24), 2));
        order.addItem(new OrderItem(new Product("SKU-1002", "ワイヤレスマウス", new BigDecimal("2800"), 6), 2));

        assertThat(order.getTotalAmount()).isEqualTo(new BigDecimal("261600"));
        assertThat(order.getTotalAmount().scale()).isZero();
    }

    @Test
    void emptyOrderTotalsZero() {
        Order order = new Order(new Customer("得意先", null), LocalDate.now(), OrderStatus.NEW);

        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

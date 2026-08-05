package com.example.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OrderDomainTest {

    @Test
    void subtotalIsScaleZeroBigDecimal() {
        OrderItem item = new OrderItem(1L, "ノートPC 14インチ", new BigDecimal("128000"), 2);

        assertThat(item.getSubtotal()).isEqualByComparingTo("256000");
        assertThat(item.getSubtotal().scale()).isZero();
    }

    @Test
    void totalAmountSumsItemsWithScaleZero() {
        Order order = new Order(new Customer("株式会社サンプル商事", null), LocalDate.of(2026, 7, 20),
                OrderStatus.SHIPPED);
        order.addItem(new OrderItem(1L, "ノートPC 14インチ", new BigDecimal("128000"), 2));
        order.addItem(new OrderItem(2L, "ワイヤレスマウス", new BigDecimal("2800"), 2));

        // 旧実装(double)の期待値 261600.0 と整合する
        assertThat(order.getTotalAmount()).isEqualByComparingTo("261600");
        assertThat(order.getTotalAmount().scale()).isZero();
    }

    @Test
    void unitPriceIsRoundedHalfUpToYen() {
        OrderItem item = new OrderItem(1L, "端数商品", new BigDecimal("100.5"), 3);

        assertThat(item.getUnitPrice()).isEqualByComparingTo("101");
        assertThat(item.getSubtotal()).isEqualByComparingTo("303");
    }

    @Test
    void orderStatusHasNewAndShipped() {
        assertThat(OrderStatus.values()).containsExactly(OrderStatus.NEW, OrderStatus.SHIPPED);
    }

    @Test
    void productPriceIsStoredAsScaleZero() {
        Product product = new Product("SKU-9", "端数", new BigDecimal("2800.4"), 5);

        assertThat(product.getPrice()).isEqualByComparingTo("2800");
        assertThat(product.getPrice().scale()).isZero();
    }
}

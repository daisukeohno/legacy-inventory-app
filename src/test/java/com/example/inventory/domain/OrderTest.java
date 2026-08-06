package com.example.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class OrderTest {

    @Test
    void subtotalIsUnitPriceTimesQuantity() {
        OrderItem item = new OrderItem(1, "ノートPC", new BigDecimal("128000.00"), 3);
        assertThat(item.getSubtotal()).isEqualByComparingTo("384000.00");
    }

    @Test
    void totalAmountIsSumOfSubtotalsWithoutRoundingError() {
        Order order = new Order("株式会社サンプル商事", LocalDate.of(2026, 7, 20), "NEW");
        order.addItem(new OrderItem(1, "商品A", new BigDecimal("0.10"), 3));
        order.addItem(new OrderItem(2, "商品B", new BigDecimal("0.20"), 1));

        assertThat(order.getTotalAmount()).isEqualByComparingTo("0.50");
    }

    @Test
    void totalAmountIsZeroForEmptyOrder() {
        Order order = new Order("得意先", LocalDate.now(), "NEW");
        assertThat(order.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

package com.example.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** 旧実装の double 計算による丸め誤差が BigDecimal 化で解消されていることを検証する。 */
class MoneyCalculationTest {

    @Test
    @DisplayName("小計は BigDecimal で正確に計算される")
    void subtotalIsExact() {
        OrderItem item = new OrderItem(1L, "テスト商品", new BigDecimal("0.10"), 3);

        assertThat(item.getSubtotal()).isEqualByComparingTo("0.30");
        // double では 0.1 * 3 = 0.30000000000000004 になる
        assertThat(item.getSubtotal().doubleValue()).isNotEqualTo(0.1d * 3);
    }

    @Test
    @DisplayName("合計金額は明細小計の BigDecimal 合計になる")
    void totalAmountSumsSubtotals() {
        Order order = new Order("株式会社サンプル商事", LocalDate.of(2026, 7, 20));
        order.addItem(new OrderItem(1L, "ノートPC 14インチ", new BigDecimal("128000.00"), 2));
        order.addItem(new OrderItem(2L, "ワイヤレスマウス", new BigDecimal("2800.00"), 2));

        assertThat(order.getTotalAmount()).isEqualByComparingTo("261600.00");
    }

    @Test
    @DisplayName("明細が無い注文の合計金額は0")
    void totalAmountOfEmptyOrderIsZero() {
        Order order = new Order("得意先", LocalDate.now());

        assertThat(order.getTotalAmount()).isEqualByComparingTo("0");
    }

    @Test
    @DisplayName("低在庫しきい値は既定10で、10未満のみ低在庫")
    void lowStockThreshold() {
        assertThat(new Product("SKU-1", "商品", new BigDecimal("100"), 9).isLowStock()).isTrue();
        assertThat(new Product("SKU-2", "商品", new BigDecimal("100"), 10).isLowStock()).isFalse();
        assertThat(new Product("SKU-3", "商品", new BigDecimal("100"), 10).isLowStock(20)).isTrue();
    }
}

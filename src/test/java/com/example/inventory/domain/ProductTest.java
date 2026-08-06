package com.example.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class ProductTest {

    @Test
    void lowStockUsesDefaultThresholdOfTen() {
        assertThat(product(9).isLowStock()).isTrue();
        assertThat(product(10).isLowStock()).isFalse();
        assertThat(product(11).isLowStock()).isFalse();
    }

    @Test
    void lowStockHonoursExternalisedThreshold() {
        assertThat(product(4).isLowStock(5)).isTrue();
        assertThat(product(5).isLowStock(5)).isFalse();
    }

    private Product product(int stock) {
        return new Product("SKU-X", "テスト商品", new BigDecimal("100.00"), stock);
    }
}

package com.example.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void parsesKnownValues() {
        assertThat(OrderStatus.valueOf("NEW")).isEqualTo(OrderStatus.NEW);
        assertThat(OrderStatus.valueOf("SHIPPED")).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void rejectsUnknownValue() {
        assertThatThrownBy(() -> OrderStatus.valueOf("CANCELLED"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

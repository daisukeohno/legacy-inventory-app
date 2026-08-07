package com.example.inventory.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 金額は円整数運用のため scale=0 / HALF_UP に統一する。
 */
public final class Money {

    public static final int SCALE = 0;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;
    public static final BigDecimal ZERO = BigDecimal.ZERO.setScale(SCALE, ROUNDING);

    private Money() {
    }

    public static BigDecimal normalize(BigDecimal value) {
        return value == null ? null : value.setScale(SCALE, ROUNDING);
    }
}

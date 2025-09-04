package org.solcation.solcation_be.domain.travel.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public final class Positioning {
    private static final MathContext MC = new MathContext(38, RoundingMode.HALF_UP);

    //중간에 삽입될때
    public static BigDecimal mid(BigDecimal a, BigDecimal b) {
        return a.add(b).divide(new BigDecimal("2"), MC);
    }

    //맨 앞 삽입
    public static BigDecimal before(BigDecimal next) {
        return next.divide(new BigDecimal("2"), MC);
    }

    //맨 뒤 삽입
    public static BigDecimal after(BigDecimal prev) {
        return prev.add(BigDecimal.ONE, MC);
    }
}

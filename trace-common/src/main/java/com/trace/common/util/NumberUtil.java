package com.trace.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 数值工具
 */
public class NumberUtil {

    private NumberUtil() {
    }

    /**
     * BigDecimal 保留 scale 位小数，null 安全
     */
    public static BigDecimal scale(BigDecimal value, int scale) {
        if (value == null) {
            return BigDecimal.ZERO.setScale(scale, RoundingMode.HALF_UP);
        }
        return value.setScale(scale, RoundingMode.HALF_UP);
    }

    public static BigDecimal nz(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}

package com.trace.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * 日期工具
 */
public class DateUtil {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DateUtil() {
    }

    public static String format(LocalDate date) {
        return date == null ? null : FMT.format(date);
    }

    public static LocalDate parse(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }
        return LocalDate.parse(text, FMT);
    }
}

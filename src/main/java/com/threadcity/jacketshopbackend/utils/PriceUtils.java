package com.threadcity.jacketshopbackend.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class PriceUtils {

    private static final Locale VIETNAM = Locale.of("vi", "VN");

    public static String formatVnd(BigDecimal price) {
        if (price == null) {
            return "0 ₫";
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(VIETNAM);
        return currencyFormat.format(price);
    }
    
    public static String formatVnd(Double price) {
        if (price == null) {
            return "0 ₫";
        }
        return formatVnd(BigDecimal.valueOf(price));
    }

    public static String formatVnd(Long price) {
        if (price == null) {
            return "0 ₫";
        }
        return formatVnd(BigDecimal.valueOf(price));
    }
}

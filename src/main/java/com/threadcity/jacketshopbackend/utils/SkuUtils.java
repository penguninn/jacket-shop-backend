package com.threadcity.jacketshopbackend.utils;

import java.text.Normalizer;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Pattern;

public class SkuUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    public static String toSlug(String input) {
        if (input == null) {
            return "";
        }
        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        return slug.toUpperCase(Locale.ENGLISH);
    }

    public static String generateBaseSku(String productName, String size, String color, String material) {
        return String.format("%s-%s-%s-%s",
                toSlug(productName),
                toSlug(size),
                toSlug(color),
                toSlug(material));
    }

    public static String generateUniqueSku(String baseSku, Function<String, Boolean> existsChecker) {
        String sku = baseSku;
        int counter = 1;
        while (existsChecker.apply(sku)) {
            sku = baseSku + "-" + counter;
            counter++;
        }
        return sku;
    }
}

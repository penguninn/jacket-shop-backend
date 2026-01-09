package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Sale;
import com.threadcity.jacketshopbackend.entity.SaleVariant;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Comparator;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PricingService {

    public PriceResult calculatePrice(ProductVariant variant) {
        log.debug("PricingService::calculatePrice - variantId: {}", variant.getId());

        BigDecimal originalPrice = variant.getPrice();
        Sale activeSale = findBestActiveSale(variant).orElse(null);

        if (activeSale == null) {
            log.debug("PricingService::calculatePrice - No active sale for variantId: {}", variant.getId());
            return new PriceResult(originalPrice, originalPrice, BigDecimal.ZERO, null);
        }

        BigDecimal discountPercentage = activeSale.getDiscountPercentage();
        BigDecimal discountMultiplier = BigDecimal.ONE.subtract(
                discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal finalPrice = originalPrice.multiply(discountMultiplier)
                .setScale(0, RoundingMode.HALF_UP); // VND không có decimal

        log.info("PricingService::calculatePrice - variantId: {}, originalPrice: {}, finalPrice: {}, discount: {}%",
                variant.getId(), originalPrice, finalPrice, discountPercentage);

        return new PriceResult(originalPrice, finalPrice, discountPercentage, activeSale.getId());
    }

    public Optional<Sale> findBestActiveSale(ProductVariant variant) {
        log.debug("PricingService::findBestActiveSale - variantId: {}", variant.getId());

        if (variant.getSaleVariants() == null || variant.getSaleVariants().isEmpty()) {
            return Optional.empty();
        }

        Instant now = Instant.now();

        return variant.getSaleVariants().stream()
                .map(SaleVariant::getSale)
                .filter(sale -> isSaleActive(sale, now))
                .max(Comparator.comparing(Sale::getDiscountPercentage));
    }

    public boolean isSaleActive(Sale sale) {
        return isSaleActive(sale, Instant.now());
    }

    public boolean isSaleActive(Sale sale, Instant at) {
        if (sale == null) {
            return false;
        }

        if (sale.getStatus() != Enums.Status.ACTIVE) {
            return false;
        }

        boolean afterStart = sale.getStartDate() == null || !at.isBefore(sale.getStartDate());
        boolean beforeEnd = sale.getEndDate() == null || !at.isAfter(sale.getEndDate());

        return afterStart && beforeEnd;
    }

    public BigDecimal calculateDiscountAmount(BigDecimal originalPrice, Sale sale) {
        if (sale == null || !isSaleActive(sale)) {
            return BigDecimal.ZERO;
        }

        return originalPrice.multiply(sale.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    @Value
    public static class PriceResult {

        BigDecimal originalPrice;

        BigDecimal finalPrice;

        BigDecimal discountPercentage;

        Long appliedSaleId;

        public boolean hasSale() {
            return appliedSaleId != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0;
        }

        public BigDecimal getDiscountAmount() {
            return originalPrice.subtract(finalPrice);
        }
    }
}

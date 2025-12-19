package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Sale;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {
        SizeMapper.class,
        ColorMapper.class,
        MaterialMapper.class
})
public interface ProductVariantMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(target = "salePrice", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    ProductVariantResponse toDto(ProductVariant productVariant);

    @AfterMapping
    default void mapSaleDetails(ProductVariant source, @MappingTarget ProductVariantResponse target) {
        List<Sale> sales = source.getSales();
        if (sales != null && !sales.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            
            Sale bestSale = sales.stream()
                .filter(sale -> {
                    if (sale.getDiscountPercentage() == null) return false;
                    boolean startOk = sale.getStartDate() == null || !now.isBefore(sale.getStartDate());
                    boolean endOk = sale.getEndDate() == null || !now.isAfter(sale.getEndDate());
                    return startOk && endOk;
                })
                .max(Comparator.comparing(Sale::getDiscountPercentage))
                .orElse(null);

            if (bestSale != null) {
                target.setDiscountPercentage(bestSale.getDiscountPercentage());
                if (source.getPrice() != null) {
                    BigDecimal discountFactor = bestSale.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal discountAmount = source.getPrice().multiply(discountFactor);
                    target.setSalePrice(source.getPrice().subtract(discountAmount));
                }
            }
        }
    }
}

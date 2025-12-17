package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Sale;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Mapper(componentModel = "spring", uses = {
        ProductMapper.class,
        SizeMapper.class,
        ColorMapper.class,
        MaterialMapper.class
})
public interface ProductVariantMapper {

    ProductVariantResponse toDto(ProductVariant productVariant);

    @AfterMapping
    default void mapSaleDetails(ProductVariant source, @MappingTarget ProductVariantResponse target) {
        Sale sale = source.getSale();
        if (sale != null && sale.getDiscountPercentage() != null) {
            LocalDateTime now = LocalDateTime.now();
            boolean isActive = (sale.getStartDate() == null || !now.isBefore(sale.getStartDate())) &&
                               (sale.getEndDate() == null || !now.isAfter(sale.getEndDate()));
            
            if (isActive) {
                target.setDiscountPercentage(sale.getDiscountPercentage());
                if (source.getPrice() != null) {
                    BigDecimal discountFactor = sale.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal discountAmount = source.getPrice().multiply(discountFactor);
                    target.setSalePrice(source.getPrice().subtract(discountAmount));
                }
            }
        }
    }
}

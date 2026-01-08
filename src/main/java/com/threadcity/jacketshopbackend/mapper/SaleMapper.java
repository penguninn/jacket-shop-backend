package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.promotion.request.SaleCreateRequest;
import com.threadcity.jacketshopbackend.dto.promotion.request.SaleUpdateRequest;
import com.threadcity.jacketshopbackend.dto.promotion.response.SaleResponse;
import com.threadcity.jacketshopbackend.entity.Sale;
import com.threadcity.jacketshopbackend.entity.SaleVariant;
import org.mapstruct.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface SaleMapper {

    @Mapping(target = "variants", source = "saleVariants")
    SaleResponse toDto(Sale sale);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "saleVariants", ignore = true)
    Sale toEntity(SaleCreateRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "saleVariants", ignore = true)
    void updateEntity(SaleUpdateRequest request, @MappingTarget Sale sale);

    default List<SaleResponse.SaleVariantDetail> saleVariantsToDetails(java.util.Set<SaleVariant> saleVariants) {
        if (saleVariants == null) {
            return null;
        }

        return saleVariants.stream()
                .map(this::saleVariantToDetail)
                .collect(Collectors.toList());
    }

    default SaleResponse.SaleVariantDetail saleVariantToDetail(SaleVariant saleVariant) {
        if (saleVariant == null || saleVariant.getProductVariant() == null) {
            return null;
        }

        var variant = saleVariant.getProductVariant();
        var sale = saleVariant.getSale();
        BigDecimal originalPrice = variant.getPrice();
        BigDecimal discountPercentage = sale.getDiscountPercentage();

        // Calculate sale price: price * (1 - discountPercentage/100)
        BigDecimal salePrice = originalPrice.multiply(
                BigDecimal.ONE.subtract(discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
        ).setScale(2, RoundingMode.HALF_UP);

        return SaleResponse.SaleVariantDetail.builder()
                .variantId(variant.getId())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .sku(variant.getSku())
                .image(variant.getImage())
                .originalPrice(originalPrice)
                .salePrice(salePrice)
                .build();
    }
}

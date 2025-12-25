package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.OrderDetailResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import com.threadcity.jacketshopbackend.entity.Sale;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.id", target = "productId")
    @Mapping(source = "originalPrice", target = "price")
    @Mapping(source = "price", target = "salePrice")
    @Mapping(source = "discountPercentage", target = "discountPercentage")
    OrderDetailResponse toDetailDto(OrderDetail orderDetail);

    @AfterMapping
    default void fillMissingSaleDetails(OrderDetail source, @MappingTarget OrderDetailResponse target) {
        // Fallback for price (original) if originalPrice was null in DB
        if (target.getPrice() == null && source.getProductVariant() != null) {
            target.setPrice(source.getProductVariant().getPrice());
        }

        // Fallback for discountPercentage if it was null in DB
        if (target.getDiscountPercentage() == null) {
            if (target.getPrice() != null && target.getSalePrice() != null 
                && target.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                
                BigDecimal diff = target.getPrice().subtract(target.getSalePrice());
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    // Calculate percentage with higher precision first, then round for display
                    BigDecimal percentage = diff.multiply(BigDecimal.valueOf(100))
                            .divide(target.getPrice(), 4, java.math.RoundingMode.HALF_UP)
                            .setScale(2, java.math.RoundingMode.HALF_UP); // Round for display
                    target.setDiscountPercentage(percentage);
                } else {
                    target.setDiscountPercentage(BigDecimal.ZERO);
                }
            } else {
                target.setDiscountPercentage(BigDecimal.ZERO);
            }
        }
    }

    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    @Mapping(source = "user.id", target = "userId")
    OrderResponse toDto(Order order);
}

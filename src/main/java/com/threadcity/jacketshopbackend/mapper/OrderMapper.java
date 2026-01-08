package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.order.response.OrderDetailResponse;
import com.threadcity.jacketshopbackend.dto.order.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.id", target = "productId")
    @Mapping(source = "originalPrice", target = "originalPrice")
    @Mapping(source = "price", target = "price")
    @Mapping(source = "discountPercentage", target = "discountPercentage")
    OrderDetailResponse toDetailDto(OrderDetail orderDetail);

    @AfterMapping
    default void fillMissingSaleDetails(OrderDetail source, @MappingTarget OrderDetailResponse target) {
        // Fallback for originalPrice if it was null in DB
        if (target.getOriginalPrice() == null && source.getProductVariant() != null) {
            target.setOriginalPrice(source.getProductVariant().getPrice());
        }

        // Fallback for discountPercentage if it was null in DB
        if (target.getDiscountPercentage() == null) {
            if (target.getOriginalPrice() != null && target.getPrice() != null
                && target.getOriginalPrice().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal diff = target.getOriginalPrice().subtract(target.getPrice());
                if (diff.compareTo(BigDecimal.ZERO) > 0) {
                    // Calculate percentage with higher precision first, then round for display
                    BigDecimal percentage = diff.multiply(BigDecimal.valueOf(100))
                            .divide(target.getOriginalPrice(), 4, java.math.RoundingMode.HALF_UP)
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
    @Mapping(source = "coupon.id", target = "couponId")
    @Mapping(source = "orderDetails", target = "details")
    OrderResponse toDto(Order order);
}

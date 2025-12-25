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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.id", target = "productId")
    @Mapping(source = "productVariant.price", target = "price")
    @Mapping(source = "price", target = "salePrice")
    @Mapping(target = "discountPercentage", ignore = true)
    OrderDetailResponse toDetailDto(OrderDetail orderDetail);

    @AfterMapping
    default void mapSaleDetails(OrderDetail source, @MappingTarget OrderDetailResponse target) {
        if (source.getProductVariant() != null) {
            List<Sale> sales = source.getProductVariant().getSales();
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
                }
            }
        }
    }

    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    @Mapping(source = "user.id", target = "userId")
    OrderResponse toDto(Order order);
}

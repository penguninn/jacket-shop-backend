package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.OrderDetailResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "productVariant.id", target = "productVariantId")
    @Mapping(source = "productVariant.product.id", target = "productId")
    OrderDetailResponse toDetailDto(OrderDetail orderDetail);

    @Mapping(source = "staff.id", target = "staffId")
    @Mapping(source = "staff.fullName", target = "staffName")
    @Mapping(source = "paymentMethod.id", target = "paymentMethodId")
    OrderResponse toDto(Order order);
}

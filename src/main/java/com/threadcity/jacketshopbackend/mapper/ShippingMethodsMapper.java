package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.ShippingMethodsRequest;
import com.threadcity.jacketshopbackend.dto.response.ShippingMethodsResponse;
import com.threadcity.jacketshopbackend.entity.ShippingMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShippingMethodsMapper {

    ShippingMethodsResponse toDto(ShippingMethod shippingMethod);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ShippingMethod toEntity(ShippingMethodsRequest request);
}

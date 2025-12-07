package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.PaymentMethodRequest;
import com.threadcity.jacketshopbackend.dto.response.PaymentMethodResponse;
import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {

    @Mapping(target = "configJson", ignore = true)
    PaymentMethodResponse toDto(PaymentMethod paymentMethod);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentMethod toEntity(PaymentMethodRequest request);
}

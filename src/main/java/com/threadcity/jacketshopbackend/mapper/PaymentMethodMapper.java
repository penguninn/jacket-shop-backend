package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.payment.request.PaymentMethodCreateRequest;
import com.threadcity.jacketshopbackend.dto.payment.request.PaymentMethodUpdateRequest;
import com.threadcity.jacketshopbackend.dto.payment.response.PaymentMethodResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {

    PaymentMethodResponse toDto(PaymentMethod paymentMethod);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    PaymentMethod toEntity(PaymentMethodCreateRequest request);
}

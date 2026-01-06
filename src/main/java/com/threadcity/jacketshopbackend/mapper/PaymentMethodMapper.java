package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.payment.request.PaymentMethodCreateRequest;
import com.threadcity.jacketshopbackend.dto.payment.request.PaymentMethodUpdateRequest;
import com.threadcity.jacketshopbackend.dto.payment.response.PaymentMethodResponse;
import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentMethodMapper {

    PaymentMethodResponse toResponse(PaymentMethod entity);

    List<PaymentMethodResponse> toResponseList(List<PaymentMethod> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    PaymentMethod toEntity(PaymentMethodCreateRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    void updateEntity(PaymentMethodUpdateRequest request, @MappingTarget PaymentMethod entity);
}

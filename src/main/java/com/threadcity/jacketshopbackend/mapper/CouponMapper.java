package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.request.CouponRequest;
import com.threadcity.jacketshopbackend.dto.response.CouponResponse;
import com.threadcity.jacketshopbackend.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    CouponResponse toDto(Coupon coupon);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    Coupon toEntity(CouponRequest request);
}

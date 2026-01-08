package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.promotion.request.CouponCreateRequest;
import com.threadcity.jacketshopbackend.dto.promotion.request.CouponUpdateRequest;
import com.threadcity.jacketshopbackend.dto.promotion.response.CouponResponse;
import com.threadcity.jacketshopbackend.entity.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CouponMapper {
    CouponResponse toDto(Coupon coupon);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "userCoupons", ignore = true)
    Coupon toEntity(CouponCreateRequest request);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "usedCount", ignore = true)
    @Mapping(target = "userCoupons", ignore = true)
    void updateEntity(CouponUpdateRequest request, @MappingTarget Coupon coupon);
}

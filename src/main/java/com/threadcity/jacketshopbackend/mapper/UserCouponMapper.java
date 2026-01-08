package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.promotion.response.UserCouponResponse;
import com.threadcity.jacketshopbackend.entity.UserCoupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = {CouponMapper.class})
public interface UserCouponMapper {

    @Mapping(target = "userId", source = "user.id")
    UserCouponResponse toResponse(UserCoupon entity);

    List<UserCouponResponse> toResponseList(List<UserCoupon> entities);
}

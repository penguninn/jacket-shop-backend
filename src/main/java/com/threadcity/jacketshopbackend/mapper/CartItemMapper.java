package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.cart.response.CartItemResponse;
import com.threadcity.jacketshopbackend.entity.CartItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProductVariantMapper.class})
public interface CartItemMapper {

    CartItemResponse toResponse(CartItem entity);

    List<CartItemResponse> toResponseList(List<CartItem> entities);
}

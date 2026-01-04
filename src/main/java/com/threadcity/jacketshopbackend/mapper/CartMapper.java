package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.cart.response.CartItemResponse;
import com.threadcity.jacketshopbackend.dto.cart.response.CartResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductVariantMapper.class})
public interface CartMapper {

    CartItemResponse toCartItemResponse(CartItem cartItem);

    CartResponse toCartResponse(Cart cart);
}

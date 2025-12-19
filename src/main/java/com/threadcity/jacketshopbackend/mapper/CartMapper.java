package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.response.CartItemResponse;
import com.threadcity.jacketshopbackend.dto.response.CartResponse;
import com.threadcity.jacketshopbackend.entity.Cart;
import com.threadcity.jacketshopbackend.entity.CartItem;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {ProductVariantMapper.class})
public interface CartMapper {

    CartItemResponse toCartItemResponse(CartItem cartItem);

    CartResponse toCartResponse(Cart cart);
}

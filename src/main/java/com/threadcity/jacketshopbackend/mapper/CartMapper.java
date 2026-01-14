package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.cart.response.CartItemResponse;
import com.threadcity.jacketshopbackend.dto.cart.response.CartResponse;
import com.threadcity.jacketshopbackend.entity.Cart;
import com.threadcity.jacketshopbackend.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ProductVariantMapper.class})
public interface CartMapper {

    @Mapping(target = "price", ignore = true)
    @Mapping(target = "originalPrice", ignore = true)
    @Mapping(target = "discountPercentage", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "totalItems", ignore = true)
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "selectedItemsCount", ignore = true)
    @Mapping(target = "selectedItemsTotal", ignore = true)
    CartResponse toCartResponse(Cart cart);
}

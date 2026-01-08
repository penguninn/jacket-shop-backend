package com.threadcity.jacketshopbackend.mapper;

import com.threadcity.jacketshopbackend.dto.cart.response.CartItemResponse;
import com.threadcity.jacketshopbackend.dto.cart.response.CartResponse;
import com.threadcity.jacketshopbackend.entity.Cart;
import com.threadcity.jacketshopbackend.entity.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {ProductVariantMapper.class})
public interface CartMapper {

    CartItemResponse toCartItemResponse(CartItem cartItem);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", source = "cartItems")
    @Mapping(target = "totalItems", expression = "java(calculateTotalItems(cart))")
    @Mapping(target = "totalPrice", expression = "java(calculateTotalPrice(cart))")
    @Mapping(target = "selectedItemsCount", expression = "java(calculateSelectedItemsCount(cart))")
    @Mapping(target = "selectedItemsTotal", expression = "java(calculateSelectedItemsTotal(cart))")
    CartResponse toCartResponse(Cart cart);

    default Integer calculateTotalItems(Cart cart) {
        return cart.getCartItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal calculateTotalPrice(Cart cart) {
        return cart.getCartItems().stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    default Integer calculateSelectedItemsCount(Cart cart) {
        return cart.getCartItems().stream()
                .filter(CartItem::getSelected)
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    default BigDecimal calculateSelectedItemsTotal(Cart cart) {
        return cart.getCartItems().stream()
                .filter(CartItem::getSelected)
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

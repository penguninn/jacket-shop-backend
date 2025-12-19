package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.response.CartResponse;
import com.threadcity.jacketshopbackend.entity.Cart;
import com.threadcity.jacketshopbackend.entity.CartItem;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.CartMapper;
import com.threadcity.jacketshopbackend.repository.CartItemRepository;
import com.threadcity.jacketshopbackend.repository.CartRepository;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    public CartResponse getCart() {
        log.info("CartService::getCart - Execution started.");
        Cart cart = getOrCreateCart();
        log.info("CartService::getCart - Execution completed.");
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse addToCart(CartItemRequest request) {
        log.info("CartService::addToCart - Execution started.");
        Cart cart = getOrCreateCart();

        ProductVariant variant = productVariantRepository.findById(request.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product variant not found"));

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(request.getProductVariantId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variant)
                    .quantity(request.getQuantity())
                    .build();
            cart.getItems().add(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        log.info("CartService::addToCart - Execution completed.");
        return cartMapper.toCartResponse(savedCart);
    }

    @Transactional
    public CartResponse updateCartItem(Long itemId, Integer quantity) {
        log.info("CartService::updateCartItem - Execution started. [itemId: {}]", itemId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Cart item not found"));

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        log.info("CartService::updateCartItem - Execution completed.");
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(Long itemId) {
        log.info("CartService::removeCartItem - Execution started. [itemId: {}]", itemId);
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Cart item not found"));

        Cart cart = cartItem.getCart();
        cart.getItems().remove(cartItem);
        cartRepository.save(cart);

        log.info("CartService::removeCartItem - Execution completed.");
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public void clearCart() {
        log.info("CartService::clearCart - Execution started.");
        Cart cart = getOrCreateCart();
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("CartService::clearCart - Execution completed.");
    }

    private Cart getOrCreateCart() {
        Long userId = getUserId();
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));
            Cart newCart = Cart.builder()
                    .user(user)
                    .items(new ArrayList<>())
                    .build();
            return cartRepository.save(newCart);
        });
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}

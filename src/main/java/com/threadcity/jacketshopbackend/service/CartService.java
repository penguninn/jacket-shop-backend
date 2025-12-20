package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.response.CartResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.Cart;
import com.threadcity.jacketshopbackend.entity.CartItem;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.AuthorizationFailedException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
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

import com.threadcity.jacketshopbackend.dto.response.CartValidationResponse;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;
    private final ProductVariantService productVariantService;

    public CartResponse getCart() {
        log.info("CartService::getCart - Execution started.");
        Cart cart = getOrCreateCart();
        log.info("CartService::getCart - Execution completed.");
        return cartMapper.toCartResponse(cart);
    }

    public Integer countMyCartItems() {
        log.info("CartService::countMyCartItems - Execution started.");
        Long userId = getUserId();
        Integer count = cartItemRepository.countByCartUserId(userId);
        log.info("CartService::countMyCartItems - Execution completed.");
        return count != null ? count : 0;
    }

    @Transactional(readOnly = true)
    public CartValidationResponse validateCartBeforeCheckout() {
        log.info("CartService::validateCartBeforeCheckout - Execution started.");
        Cart cart = getOrCreateCart();
        List<CartValidationResponse.CartIssue> issues = new ArrayList<>();

        for (CartItem item : cart.getItems()) {
            ProductVariant variant = item.getProductVariant();

            if (variant.getStatus() != Status.ACTIVE) {
                issues.add(CartValidationResponse.CartIssue.builder()
                        .productVariantId(variant.getId())
                        .productName(variant.getProduct().getName())
                        .issueType("INACTIVE")
                        .message("Product is no longer available.")
                        .build());
                continue;
            }

            if (variant.getAvailableQuantity() < item.getQuantity()) {
                issues.add(CartValidationResponse.CartIssue.builder()
                        .productVariantId(variant.getId())
                        .productName(variant.getProduct().getName())
                        .issueType("OUT_OF_STOCK")
                        .message("Not enough stock. Available: " + variant.getAvailableQuantity())
                        .build());
            }
        }

        log.info("CartService::validateCartBeforeCheckout - Execution completed.");
        return CartValidationResponse.builder()
                .isValid(issues.isEmpty())
                .issues(issues)
                .build();
    }

    @Transactional
    public CartResponse addToCart(CartItemRequest request) {
        log.info("CartService::addToCart - Execution started.");
        Cart cart = getOrCreateCart();

        int quantityToAdd = request.getQuantity();
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductVariant().getId().equals(request.getProductVariantId()))
                .findFirst();

        if (existingItem.isPresent()) {
            quantityToAdd += existingItem.get().getQuantity();
        }

        if (!productVariantService.isVariantAvailable(request.getProductVariantId(), quantityToAdd)) {
             throw new InvalidRequestException(ErrorCodes.PRODUCT_OUT_OF_STOCK, "Not enough stock or variant unavailable");
        }

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(quantityToAdd);
        } else {
            ProductVariant variantRef = productVariantRepository.getReferenceById(request.getProductVariantId());
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .productVariant(variantRef)
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
        Long userId = getUserId();
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.CART_ITEM_NOT_FOUND, "Cart item not found"));

        // 1. Security Check
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
             throw new AuthorizationFailedException(ErrorCodes.ACCESS_DENIED, "You do not own this cart item");
        }

        // 2. Handle <= 0 quantity as remove
        if (quantity <= 0) {
            return removeCartItem(itemId);
        }

        // 3. Check stock availability
        if (!productVariantService.isVariantAvailable(cartItem.getProductVariant().getId(), quantity)) {
             throw new InvalidRequestException(ErrorCodes.PRODUCT_OUT_OF_STOCK, "Not enough stock or variant unavailable");
        }

        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        log.info("CartService::updateCartItem - Execution completed.");
        return cartMapper.toCartResponse(cart);
    }

    @Transactional
    public CartResponse removeCartItem(Long itemId) {
        log.info("CartService::removeCartItem - Execution started. [itemId: {}]", itemId);
        Long userId = getUserId();
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.CART_ITEM_NOT_FOUND, "Cart item not found"));

        // 1. Security Check
        if (!cartItem.getCart().getUser().getId().equals(userId)) {
             throw new AuthorizationFailedException(ErrorCodes.ACCESS_DENIED, "You do not own this cart item");
        }

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

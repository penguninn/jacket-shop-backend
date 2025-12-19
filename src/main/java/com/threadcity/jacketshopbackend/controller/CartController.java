package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.CartResponse;
import com.threadcity.jacketshopbackend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<?> getCart() {
        log.info("CartController::getCart - Execution started");
        CartResponse response = cartService.getCart();
        log.info("CartController::getCart - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get cart successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> addToCart(@Valid @RequestBody CartItemRequest request) {
        log.info("CartController::addToCart - Execution started");
        CartResponse response = cartService.addToCart(request);
        log.info("CartController::addToCart - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Item added to cart successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<?> updateCartItem(@PathVariable Long itemId, @RequestParam Integer quantity) {
        log.info("CartController::updateCartItem - Execution started");
        CartResponse response = cartService.updateCartItem(itemId, quantity);
        log.info("CartController::updateCartItem - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Cart item updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<?> removeCartItem(@PathVariable Long itemId) {
        log.info("CartController::removeCartItem - Execution started");
        CartResponse response = cartService.removeCartItem(itemId);
        log.info("CartController::removeCartItem - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Item removed from cart successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping
    public ApiResponse<?> clearCart() {
        log.info("CartController::clearCart - Execution started");
        cartService.clearCart();
        log.info("CartController::clearCart - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Cart cleared successfully.")
                .timestamp(Instant.now())
                .build();
    }
}

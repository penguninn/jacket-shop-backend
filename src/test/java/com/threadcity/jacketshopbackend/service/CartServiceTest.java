package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.response.CartResponse;
import com.threadcity.jacketshopbackend.dto.response.CartValidationResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.AuthorizationFailedException;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.CartMapper;
import com.threadcity.jacketshopbackend.repository.CartItemRepository;
import com.threadcity.jacketshopbackend.repository.CartRepository;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    CartRepository cartRepository;

    @Mock
    CartItemRepository cartItemRepository;

    @Mock
    ProductVariantRepository productVariantRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    CartMapper cartMapper;

    @Mock
    ProductVariantService productVariantService;

    @InjectMocks
    CartService cartService;

    private User user;
    private Cart cart;

    @BeforeEach
    void setupSecurityContext() {
        UserDetailsImpl userDetails = mock(UserDetailsImpl.class);
        when(userDetails.getId()).thenReturn(1L);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(userDetails, null)
        );

        user = User.builder().id(1L).build();

        cart = Cart.builder()
                .user(user)
                .items(new ArrayList<>())
                .build();
    }


    /* ===================== getCart ===================== */

    @Test
    void getCart_existingCart_success() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartMapper.toCartResponse(cart)).thenReturn(new CartResponse());

        CartResponse result = cartService.getCart();

        assertNotNull(result);
        verify(cartRepository).findByUserId(1L);
    }

    /* ===================== countMyCartItems ===================== */

    @Test
    void countMyCartItems_success() {
        when(cartItemRepository.countByCartUserId(1L)).thenReturn(3);

        Integer count = cartService.countMyCartItems();

        assertEquals(3, count);
    }

    /* ===================== validateCartBeforeCheckout ===================== */

    @Test
    void validateCart_withOutOfStockItem_shouldReturnIssue() {
        Product product = Product.builder().name("Jacket").build();
        ProductVariant variant = ProductVariant.builder()
                .id(10L)
                .status(Status.ACTIVE)
                .availableQuantity(1)
                .product(product)
                .build();

        CartItem item = CartItem.builder()
                .productVariant(variant)
                .quantity(5)
                .build();

        cart.getItems().add(item);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartValidationResponse response = cartService.validateCartBeforeCheckout();

        assertFalse(response.isValid());
        assertEquals(1, response.getIssues().size());
        assertEquals("OUT_OF_STOCK", response.getIssues().get(0).getIssueType());
    }

    /* ===================== addToCart ===================== */

    @Test
    void addToCart_success() {
        CartItemRequest request = new CartItemRequest();
        request.setProductVariantId(10L);
        request.setQuantity(2);

        ProductVariant variant = ProductVariant.builder().id(10L).build();

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productVariantService.isVariantAvailable(10L, 2)).thenReturn(true);
        when(productVariantRepository.getReferenceById(10L)).thenReturn(variant);
        when(cartRepository.save(any())).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(new CartResponse());

        CartResponse result = cartService.addToCart(request);

        assertNotNull(result);
        assertEquals(1, cart.getItems().size());
    }

    @Test
    void addToCart_outOfStock_shouldThrowException() {
        CartItemRequest request = new CartItemRequest();
        request.setProductVariantId(10L);
        request.setQuantity(5);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(productVariantService.isVariantAvailable(10L, 5)).thenReturn(false);

        assertThrows(InvalidRequestException.class,
                () -> cartService.addToCart(request));
    }

    /* ===================== updateCartItem ===================== */

    @Test
    void updateCartItem_success() {
        ProductVariant variant = ProductVariant.builder().id(10L).build();

        CartItem item = CartItem.builder()
                .id(100L)
                .cart(cart)
                .productVariant(variant)
                .quantity(1)
                .build();

        cart.getItems().add(item);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(productVariantService.isVariantAvailable(10L, 3)).thenReturn(true);
        when(cartMapper.toCartResponse(cart)).thenReturn(new CartResponse());

        CartResponse result = cartService.updateCartItem(100L, 3);

        assertNotNull(result);
        assertEquals(3, item.getQuantity());
    }

    @Test
    void updateCartItem_notOwner_shouldThrowException() {
        User otherUser = User.builder().id(99L).build();
        Cart otherCart = Cart.builder().user(otherUser).build();

        CartItem item = CartItem.builder()
                .id(100L)
                .cart(otherCart)
                .build();

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));

        assertThrows(AuthorizationFailedException.class,
                () -> cartService.updateCartItem(100L, 2));
    }

    /* ===================== removeCartItem ===================== */

    @Test
    void removeCartItem_success() {
        CartItem item = CartItem.builder()
                .id(100L)
                .cart(cart)
                .build();

        cart.getItems().add(item);

        when(cartItemRepository.findById(100L)).thenReturn(Optional.of(item));
        when(cartRepository.save(cart)).thenReturn(cart);
        when(cartMapper.toCartResponse(cart)).thenReturn(new CartResponse());

        CartResponse result = cartService.removeCartItem(100L);

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());
    }

    /* ===================== clearCart ===================== */

    @Test
    void clearCart_success() {
        cart.getItems().add(new CartItem());

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(cart)).thenReturn(cart);

        cartService.clearCart();

        assertTrue(cart.getItems().isEmpty());
    }
}

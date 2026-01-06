package com.threadcity.jacketshopbackend.dto.cart.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

import com.threadcity.jacketshopbackend.dto.product.response.ProductVariantResponse;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItemResponse implements Serializable {
    private Long id;
    private ProductVariantResponse productVariant;
    private Integer quantity;
    private Boolean selected;
    private BigDecimal unitPrice;
    private Instant createdAt;
    private Instant updatedAt;
}

package com.threadcity.jacketshopbackend.dto.cart.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse implements Serializable {
    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private Integer totalItems;
    private BigDecimal totalPrice;
    private Integer selectedItemsCount;
    private BigDecimal selectedItemsTotal;
    private Instant createdAt;
    private Instant updatedAt;
}

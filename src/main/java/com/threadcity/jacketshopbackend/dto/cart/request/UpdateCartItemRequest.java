package com.threadcity.jacketshopbackend.dto.cart.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCartItemRequest implements Serializable {

    @NotNull(message = "Cart item ID is required")
    private Long id;

    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    private Boolean selected;
}

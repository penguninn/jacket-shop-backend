package com.threadcity.jacketshopbackend.dto.cart.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkUpdateCartItemsRequest implements Serializable {

    @NotEmpty(message = "Cart item IDs list cannot be empty")
    private List<Long> cartItemIds;

    @NotNull(message = "Selected status is required")
    private Boolean selected;
}

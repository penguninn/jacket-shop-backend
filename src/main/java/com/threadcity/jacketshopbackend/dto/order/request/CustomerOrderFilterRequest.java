package com.threadcity.jacketshopbackend.dto.order.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Filter request for customer's own orders
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderFilterRequest implements Serializable {

    private List<Enums.OrderStatus> statuses;

    private Instant startDate;

    private Instant endDate;

    @Builder.Default
    @Min(value = 0, message = "Page must be non-negative")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 20;

    @Builder.Default
    private String sortBy = "createdAt";

    @Builder.Default
    private String sortDir = "DESC";
}

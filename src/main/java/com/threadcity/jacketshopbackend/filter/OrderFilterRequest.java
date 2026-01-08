package com.threadcity.jacketshopbackend.filter;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest implements Serializable {

    @Builder.Default
    @Min(value = 0, message = "Page must be non-negative")
    private Integer page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be at least 1")
    @Max(value = 100, message = "Size must not exceed 100")
    private Integer size = 10;

    @Builder.Default
    @Pattern(regexp = "id|orderCode|createdAt|updatedAt|total|status", message = "Invalid sort field")
    private String sortBy = "createdAt";

    @Builder.Default
    @Pattern(regexp = "ASC|DESC|asc|desc", message = "Sort direction must be ASC or DESC")
    private String sortDir = "desc";

    @Size(max = 32, message = "Order code must not exceed 32 characters")
    private String orderCode;

    private Instant startDate;

    private Instant endDate;

    private OrderType orderType;

    private OrderStatus status;

    private PaymentStatus paymentStatus;

    private Long userId;

    private Long staffId;
}

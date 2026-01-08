package com.threadcity.jacketshopbackend.dto.promotion.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Request DTO for admin to assign coupon to user
 */
@Data
@Builder
public class UserCouponAssignRequest implements Serializable {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Coupon ID is required")
    private Long couponId;
}

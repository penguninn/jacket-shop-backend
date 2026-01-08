package com.threadcity.jacketshopbackend.dto.promotion.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

/**
 * Request DTO for user to claim coupon by code
 * User ID will be taken from authentication context
 */
@Data
@Builder
public class UserCouponClaimRequest implements Serializable {

    @NotBlank(message = "Coupon code is required")
    @Size(max = 50, message = "Coupon code must be less than 50 characters")
    private String couponCode;
}

package com.threadcity.jacketshopbackend.dto.promotion.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserCouponResponse implements Serializable {

    private Long id;
    private Long userId;
    private CouponResponse coupon;
    private Integer usedCount;
    private Instant firstUsedAt;
    private Instant lastUsedAt;
    private Instant createdAt;
    private Instant updatedAt;
}

package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class CouponResponse implements Serializable {
    private Long id;
    private String code;
    private String description;
    private Enums.CouponType type;
    private BigDecimal value;
    private BigDecimal minOrderValue;
    private BigDecimal maxDiscount;
    private Integer usageLimit;
    private Integer usedCount;
    private Instant validFrom;
    private Instant validTo;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

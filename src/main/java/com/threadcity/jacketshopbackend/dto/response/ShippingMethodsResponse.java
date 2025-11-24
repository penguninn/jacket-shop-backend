package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class ShippingMethodsResponse implements Serializable {

    private Long id;

    private String name;

    private String description;

    private BigDecimal fee;

    private Integer estimatedDays;

    private Enums.Status status;

    private Instant createdAt;

    private Instant updatedAt;
}

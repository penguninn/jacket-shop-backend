package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
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
public class PaymentMethodResponse implements Serializable {

    private Long id;

    private String name;

    private String code;

    private Enums.PaymentMethodType type;

    private String description;

    private String config;

    private Enums.Status status;

    private Instant createdAt;

    private Instant updatedAt;
}
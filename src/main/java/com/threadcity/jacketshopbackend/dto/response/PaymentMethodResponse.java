package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PaymentMethodResponse implements Serializable {

    private Long id;

    private String name;

    private String code;

    private List<Enums.PaymentMethodType> types;

    private String description;

    private String config;

    private Enums.Status status;

    private Instant createdAt;

    private Instant updatedAt;
}

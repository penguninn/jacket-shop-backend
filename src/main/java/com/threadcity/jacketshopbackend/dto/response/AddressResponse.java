package com.threadcity.jacketshopbackend.dto.response;

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
public class AddressResponse implements Serializable {

    private Long id;

    private String addressLine;

    private WardResponse ward;

    private DistrictResponse district;

    private ProvinceResponse province;

    private Boolean isDefault;

    private String recipientName;

    private String recipientPhone;

    private String label;

    private Instant createdAt;

    private Instant updatedAt;
}

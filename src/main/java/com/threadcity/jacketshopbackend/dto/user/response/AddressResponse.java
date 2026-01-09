package com.threadcity.jacketshopbackend.dto.user.response;

import com.threadcity.jacketshopbackend.dto.location.response.DistrictResponse;
import com.threadcity.jacketshopbackend.dto.location.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.dto.location.response.WardResponse;
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

    private Instant createdAt;

    private Instant updatedAt;
}

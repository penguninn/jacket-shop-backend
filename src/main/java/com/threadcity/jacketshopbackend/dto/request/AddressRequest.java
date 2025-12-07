package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressRequest implements Serializable {

    private String addressLine;

    private Long wardId;

    private Long districtId;

    private Long provinceId;

    private Boolean isDefault;

    private String recipientName;

    private String recipientPhone;
}

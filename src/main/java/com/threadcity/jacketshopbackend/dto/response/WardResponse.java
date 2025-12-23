package com.threadcity.jacketshopbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class WardResponse implements Serializable {

    private Long id;

    private String name;

    private String goShipId;

    private Long districtId;
}

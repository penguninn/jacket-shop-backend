package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WardResponse implements Serializable {

    private Long id;

    private String code;

    private String name;

    private Long districtId;
}

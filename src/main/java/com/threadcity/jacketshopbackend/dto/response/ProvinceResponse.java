package com.threadcity.jacketshopbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProvinceResponse implements Serializable {

    private Long id;

    private String name;
}

package com.threadcity.jacketshopbackend.dto.goship;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoshipWard {
    private Long id;
    private String name;
    @JsonProperty("district_id")
    private String districtId;
}

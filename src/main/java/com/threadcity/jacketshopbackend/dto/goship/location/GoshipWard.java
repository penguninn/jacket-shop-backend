package com.threadcity.jacketshopbackend.dto.goship.location;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoshipWard {
    private String id;
    private String name;
    @JsonProperty("district_id")
    private String districtId;
}

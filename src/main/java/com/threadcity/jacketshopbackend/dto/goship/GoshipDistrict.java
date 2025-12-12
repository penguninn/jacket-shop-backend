package com.threadcity.jacketshopbackend.dto.goship;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoshipDistrict {
    private String id;
    private String name;
    @JsonProperty("city_id")
    private String cityId;
}

package com.threadcity.jacketshopbackend.dto.location.response;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WardResponse implements Serializable {

    private Long id;

    private String name;

    private String goshipId;

    private Long districtId;
}

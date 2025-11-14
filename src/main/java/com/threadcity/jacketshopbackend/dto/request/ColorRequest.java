package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColorRequest {
    @NotNull(message = "Name cannot be null")
    private String name;
    private String description;
    private Enums.Status status;
}

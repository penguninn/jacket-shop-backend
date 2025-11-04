package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SizeRequest {
    @NotNull(message = "Size cannot be null")
    private String name;
    private String description;
    private Enums.Status status;
}

package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StyleRequest {
    @NotNull(message = "Name cannot be null")
    private String name; // bomber, biker, hoodie, blazer, etc.
    private String description;
    private Enums.Status status;
}

package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 200, message = "Name must be less than 200 characters")
    private String name;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    private String description;

    @NotNull(message = "Style ID is required")
    private Long styleId;

    private String thumbnail;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

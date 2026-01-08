package com.threadcity.jacketshopbackend.dto.product.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandUpdateRequest {

    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 120, message = "Name must be less than 120 characters")
    private String name;

    @Size(max = 500, message = "Description too long")
    private String description;

    @Size(max = 500, message = "Logo URL too long")
    private String logo;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

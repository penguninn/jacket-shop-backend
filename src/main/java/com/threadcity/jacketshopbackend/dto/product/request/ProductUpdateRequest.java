package com.threadcity.jacketshopbackend.dto.product.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProductUpdateRequest implements Serializable {

    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 200, message = "Name must be less than 200 characters")
    private String name;

    @NotNull(message = "Brand ID is required")
    private Long brandId;

    @Size(max = 4000, message = "Description must be less than 4000 characters")
    private String description;

    @NotNull(message = "Style ID is required")
    private Long styleId;

    private String thumbnail;

    @NotNull(message = "Status is required")
    private Enums.Status status;

    @Builder.Default
    private Boolean isFeatured = false;
}
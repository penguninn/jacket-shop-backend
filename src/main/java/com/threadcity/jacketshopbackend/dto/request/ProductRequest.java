package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductRequest {
    @NotNull(message = "Name cannot be null")
    private String name;
    private Long categoryId;
    private Long brandId;
    private String description;
    private Long materialId;
    private Long styleId;
    private String thumbnail;
    private Enums.Status status;
}

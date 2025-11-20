package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
    private String imagesJson;
    private Enums.Status status;
}

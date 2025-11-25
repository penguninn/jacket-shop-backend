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
    private Long category;
    private Long brand;
    private String description;
    private Long material;
    private Long style;
    private String imagesJson;
    private Enums.Status status;
}

package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class ProductResponse implements Serializable {
    private Long id;
    private String name;
    private CategoryResponse category;
    private BrandResponse brand;
    private String description;
    private MaterialResponse material;
    private StyleResponse style;
    private String imagesJson;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

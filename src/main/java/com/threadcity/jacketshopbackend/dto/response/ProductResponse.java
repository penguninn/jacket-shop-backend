package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse implements Serializable {
    private Long id;
    private String name;
    private BrandResponse brand;
    private String description;
    private StyleResponse style;
    private String thumbnail;
    private Boolean isFeatured;
    private Long soldCount;
    private BigDecimal ratingAverage;
    private Integer ratingCount;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private List<ColorResponse> colors;
    private List<MaterialResponse> materials;
    private List<SizeResponse> sizes;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

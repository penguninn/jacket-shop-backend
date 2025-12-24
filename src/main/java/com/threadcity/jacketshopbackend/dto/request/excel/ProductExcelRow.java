package com.threadcity.jacketshopbackend.dto.request.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductExcelRow {
    private int rowNumber;
    private String name;
    private String description;
    private String brandName;
    private String styleName;
    private String thumbnail;
    private Boolean isFeatured;
}

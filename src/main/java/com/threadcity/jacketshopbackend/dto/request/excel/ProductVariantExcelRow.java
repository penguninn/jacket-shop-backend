package com.threadcity.jacketshopbackend.dto.request.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantExcelRow {
    private int rowNumber;
    private String productName;
    private String sizeName;
    private String colorName;
    private String materialName;
    private String sku;
    private BigDecimal price;
    private BigDecimal costPrice;
    private Integer quantity;
    private String image;
    private BigDecimal weight;
    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
}

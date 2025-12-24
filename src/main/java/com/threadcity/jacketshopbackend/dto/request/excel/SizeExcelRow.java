package com.threadcity.jacketshopbackend.dto.request.excel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SizeExcelRow {
    private int rowNumber;
    private String name;
    private String description;
}

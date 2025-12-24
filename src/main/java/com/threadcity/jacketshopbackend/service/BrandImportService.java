package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.excel.BrandExcelRow;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.repository.BrandRepository;
import com.threadcity.jacketshopbackend.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandImportService {

    private final BrandRepository brandRepository;

    @Transactional
    public ImportResult importBrands(MultipartFile file) {
        List<String> errorDetails = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            List<BrandExcelRow> rows = ExcelUtils.readExcel(file, (row, rowNum) -> {
                String name = ExcelUtils.getCellValueAsString(row, 0); // Column 0: Name
                String description = ExcelUtils.getCellValueAsString(row, 1); // Column 1: Description
                return BrandExcelRow.builder()
                        .rowNumber(rowNum)
                        .name(name)
                        .description(description)
                        .build();
            });

            for (BrandExcelRow row : rows) {
                try {
                    validateAndSave(row);
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    errorDetails.add("Row " + row.getRowNumber() + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            log.error("Error reading Excel file", e);
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return ImportResult.builder()
                .totalRows(successCount + errorCount)
                .successCount(successCount)
                .errorCount(errorCount)
                .errorDetails(errorDetails)
                .build();
    }

    private void validateAndSave(BrandExcelRow row) {
        if (row.getName() == null || row.getName().isBlank()) {
            throw new IllegalArgumentException("Brand Name is required");
        }
        if (brandRepository.existsByName(row.getName())) {
            throw new IllegalArgumentException("Brand '" + row.getName() + "' already exists");
        }

        Brand brand = Brand.builder()
                .name(row.getName())
                .description(row.getDescription())
                .status(Enums.Status.ACTIVE)
                .build();

        brandRepository.save(brand);
    }
}

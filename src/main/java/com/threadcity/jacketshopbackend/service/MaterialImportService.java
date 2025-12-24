package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.excel.MaterialExcelRow;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import com.threadcity.jacketshopbackend.entity.Material;
import com.threadcity.jacketshopbackend.repository.MaterialRepository;
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
public class MaterialImportService {

    private final MaterialRepository materialRepository;

    @Transactional
    public ImportResult importMaterials(MultipartFile file) {
        List<String> errorDetails = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            List<MaterialExcelRow> rows = ExcelUtils.readExcel(file, (row, rowNum) -> {
                String name = ExcelUtils.getCellValueAsString(row, 0);
                String description = ExcelUtils.getCellValueAsString(row, 1);
                return MaterialExcelRow.builder()
                        .rowNumber(rowNum)
                        .name(name)
                        .description(description)
                        .build();
            });

            for (MaterialExcelRow row : rows) {
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

    private void validateAndSave(MaterialExcelRow row) {
        if (row.getName() == null || row.getName().isBlank()) {
            throw new IllegalArgumentException("Material Name is required");
        }
        if (materialRepository.existsByName(row.getName())) {
            throw new IllegalArgumentException("Material '" + row.getName() + "' already exists");
        }

        Material material = Material.builder()
                .name(row.getName())
                .description(row.getDescription())
                .status(Enums.Status.ACTIVE)
                .build();

        materialRepository.save(material);
    }
}

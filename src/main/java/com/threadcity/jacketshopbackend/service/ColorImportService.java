package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.excel.ColorExcelRow;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
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
public class ColorImportService {

    private final ColorRepository colorRepository;

    @Transactional
    public ImportResult importColors(MultipartFile file) {
        List<String> errorDetails = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            List<ColorExcelRow> rows = ExcelUtils.readExcel(file, (row, rowNum) -> {
                String name = ExcelUtils.getCellValueAsString(row, 0);
                String description = ExcelUtils.getCellValueAsString(row, 1);
                String hexCode = ExcelUtils.getCellValueAsString(row, 2);
                return ColorExcelRow.builder()
                        .rowNumber(rowNum)
                        .name(name)
                        .description(description)
                        .hexCode(hexCode)
                        .build();
            });

            for (ColorExcelRow row : rows) {
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

    private void validateAndSave(ColorExcelRow row) {
        if (row.getName() == null || row.getName().isBlank()) {
            throw new IllegalArgumentException("Color Name is required");
        }
        if (row.getHexCode() == null || row.getHexCode().isBlank()) {
            throw new IllegalArgumentException("Hex Code is required");
        }
        if (colorRepository.existsByName(row.getName())) {
            throw new IllegalArgumentException("Color '" + row.getName() + "' already exists");
        }

        Color color = Color.builder()
                .name(row.getName())
                .description(row.getDescription())
                .hexCode(row.getHexCode())
                .status(Enums.Status.ACTIVE)
                .build();

        colorRepository.save(color);
    }
}

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.excel.StyleExcelRow;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.repository.StyleRepository;
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
public class StyleImportService {

    private final StyleRepository styleRepository;

    @Transactional
    public ImportResult importStyles(MultipartFile file) {
        List<String> errorDetails = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            List<StyleExcelRow> rows = ExcelUtils.readExcel(file, (row, rowNum) -> {
                String name = ExcelUtils.getCellValueAsString(row, 0);
                String description = ExcelUtils.getCellValueAsString(row, 1);
                return StyleExcelRow.builder()
                        .rowNumber(rowNum)
                        .name(name)
                        .description(description)
                        .build();
            });

            for (StyleExcelRow row : rows) {
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

    private void validateAndSave(StyleExcelRow row) {
        if (row.getName() == null || row.getName().isBlank()) {
            throw new IllegalArgumentException("Style Name is required");
        }
        if (styleRepository.existsByName(row.getName())) {
            throw new IllegalArgumentException("Style '" + row.getName() + "' already exists");
        }

        Style style = Style.builder()
                .name(row.getName())
                .description(row.getDescription())
                .status(Enums.Status.ACTIVE)
                .build();

        styleRepository.save(style);
    }
}

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.excel.ProductExcelRow;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.repository.BrandRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.StyleRepository;
import com.threadcity.jacketshopbackend.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductImportService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final StyleRepository styleRepository;
    private final ProductService productService;

    @Transactional
    public ImportResult importProducts(MultipartFile file) {
        List<String> errorDetails = new ArrayList<>();
        Set<Long> affectedProductIds = new HashSet<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            List<ProductExcelRow> rows = ExcelUtils.readExcel(file, (row, rowNum) -> {
                String name = ExcelUtils.getCellValueAsString(row, 0);
                String description = ExcelUtils.getCellValueAsString(row, 1);
                String brandName = ExcelUtils.getCellValueAsString(row, 2);
                String styleName = ExcelUtils.getCellValueAsString(row, 3);
                String thumbnail = ExcelUtils.getCellValueAsString(row, 4);
                
                String isFeaturedStr = ExcelUtils.getCellValueAsString(row, 5);
                Boolean isFeatured = Boolean.parseBoolean(isFeaturedStr) || "1".equals(isFeaturedStr) || "true".equalsIgnoreCase(isFeaturedStr);

                return ProductExcelRow.builder()
                        .rowNumber(rowNum)
                        .name(name)
                        .description(description)
                        .brandName(brandName)
                        .styleName(styleName)
                        .thumbnail(thumbnail)
                        .isFeatured(isFeatured)
                        .build();
            });

            for (ProductExcelRow row : rows) {
                try {
                    Product saved = validateAndSave(row);
                    affectedProductIds.add(saved.getId());
                    successCount++;
                } catch (Exception e) {
                    errorCount++;
                    errorDetails.add("Row " + row.getRowNumber() + ": " + e.getMessage());
                }
            }

            // Sync affected products
            affectedProductIds.forEach(productService::syncProductData);

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

    private Product validateAndSave(ProductExcelRow row) {
        if (row.getName() == null || row.getName().isBlank()) {
            throw new IllegalArgumentException("Product Name is required");
        }
        if (productRepository.existsByName(row.getName())) {
            throw new IllegalArgumentException("Product '" + row.getName() + "' already exists");
        }

        Brand brand = null;
        if (row.getBrandName() != null && !row.getBrandName().isBlank()) {
            brand = brandRepository.findByName(row.getBrandName())
                    .orElseThrow(() -> new IllegalArgumentException("Brand not found: " + row.getBrandName()));
        } else {
             throw new IllegalArgumentException("Brand Name is required");
        }

        Style style = null;
        if (row.getStyleName() != null && !row.getStyleName().isBlank()) {
            style = styleRepository.findByName(row.getStyleName())
                    .orElseThrow(() -> new IllegalArgumentException("Style not found: " + row.getStyleName()));
        } else {
             throw new IllegalArgumentException("Style Name is required");
        }

        Product product = Product.builder()
                .name(row.getName())
                .description(row.getDescription())
                .brand(brand)
                .style(style)
                .thumbnail(row.getThumbnail())
                .isFeatured(row.getIsFeatured())
                .status(Enums.Status.ACTIVE)
                .build();

        return productRepository.save(product);
    }
}

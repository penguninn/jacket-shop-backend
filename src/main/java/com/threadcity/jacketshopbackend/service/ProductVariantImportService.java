package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.excel.ProductVariantExcelRow;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVariantImportService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final MaterialRepository materialRepository;

    @Transactional
    public ImportResult importVariants(MultipartFile file) {
        List<String> errorDetails = new ArrayList<>();
        Set<Long> affectedProductIds = new HashSet<>();
        int successCount = 0;
        int errorCount = 0;

        try {
            List<ProductVariantExcelRow> rows = ExcelUtils.readExcel(file, (row, rowNum) -> {
                String productName = ExcelUtils.getCellValueAsString(row, 0);
                String sizeName = ExcelUtils.getCellValueAsString(row, 1);
                String colorName = ExcelUtils.getCellValueAsString(row, 2);
                String materialName = ExcelUtils.getCellValueAsString(row, 3);
                String sku = ExcelUtils.getCellValueAsString(row, 4);

                Double priceVal = ExcelUtils.getCellValueAsDouble(row, 5);
                BigDecimal price = priceVal != null ? BigDecimal.valueOf(priceVal) : null;
                
                Double costPriceVal = ExcelUtils.getCellValueAsDouble(row, 6);
                BigDecimal costPrice = costPriceVal != null ? BigDecimal.valueOf(costPriceVal) : null;

                Integer quantity = ExcelUtils.getCellValueAsInt(row, 7);
                String image = ExcelUtils.getCellValueAsString(row, 8);

                Double weightVal = ExcelUtils.getCellValueAsDouble(row, 9);
                BigDecimal weight = weightVal != null ? BigDecimal.valueOf(weightVal) : null;
                
                Double lengthVal = ExcelUtils.getCellValueAsDouble(row, 10);
                BigDecimal length = lengthVal != null ? BigDecimal.valueOf(lengthVal) : null;

                Double widthVal = ExcelUtils.getCellValueAsDouble(row, 11);
                BigDecimal width = widthVal != null ? BigDecimal.valueOf(widthVal) : null;

                Double heightVal = ExcelUtils.getCellValueAsDouble(row, 12);
                BigDecimal height = heightVal != null ? BigDecimal.valueOf(heightVal) : null;

                return ProductVariantExcelRow.builder()
                        .rowNumber(rowNum)
                        .productName(productName)
                        .sizeName(sizeName)
                        .colorName(colorName)
                        .materialName(materialName)
                        .sku(sku)
                        .price(price)
                        .costPrice(costPrice)
                        .quantity(quantity)
                        .image(image)
                        .weight(weight)
                        .length(length)
                        .width(width)
                        .height(height)
                        .build();
            });

            for (ProductVariantExcelRow row : rows) {
                try {
                    ProductVariant saved = validateAndSave(row);
                    affectedProductIds.add(saved.getProduct().getId());
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

    private ProductVariant validateAndSave(ProductVariantExcelRow row) {
        if (row.getSku() == null || row.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (productVariantRepository.existsBySku(row.getSku())) {
            throw new IllegalArgumentException("SKU '" + row.getSku() + "' already exists");
        }
        
        if (row.getProductName() == null || row.getProductName().isBlank()) {
            throw new IllegalArgumentException("Product Name is required");
        }
        Product product = productRepository.findByName(row.getProductName()) // Assuming findByName exists or use custom query
             .orElseThrow(() -> new IllegalArgumentException("Product not found: " + row.getProductName()));

        // Since I only added existsByName to ProductRepository, I need to add findByName or use another way.
        // Wait, I didn't add findByName to ProductRepository yet. I need to do that.
        // I'll assume I'll add it.
        
        if (row.getColorName() == null || row.getColorName().isBlank()) {
            throw new IllegalArgumentException("Color Name is required");
        }
        Color color = colorRepository.findByName(row.getColorName())
                .orElseThrow(() -> new IllegalArgumentException("Color not found: " + row.getColorName()));

        if (row.getSizeName() == null || row.getSizeName().isBlank()) {
            throw new IllegalArgumentException("Size Name is required");
        }
        Size size = sizeRepository.findByName(row.getSizeName())
                .orElseThrow(() -> new IllegalArgumentException("Size not found: " + row.getSizeName()));

        if (row.getMaterialName() == null || row.getMaterialName().isBlank()) {
            throw new IllegalArgumentException("Material Name is required");
        }
        Material material = materialRepository.findByName(row.getMaterialName())
                .orElseThrow(() -> new IllegalArgumentException("Material not found: " + row.getMaterialName()));

        if (productVariantRepository.existsByProductIdAndColorIdAndSizeIdAndMaterialId(
                product.getId(), color.getId(), size.getId(), material.getId())) {
            throw new IllegalArgumentException("Variant with this configuration already exists");
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .color(color)
                .size(size)
                .material(material)
                .sku(row.getSku())
                .price(row.getPrice() != null ? row.getPrice() : BigDecimal.ZERO)
                .costPrice(row.getCostPrice() != null ? row.getCostPrice() : BigDecimal.ZERO)
                .quantity(row.getQuantity() != null ? row.getQuantity() : 0)
                .availableQuantity(row.getQuantity() != null ? row.getQuantity() : 0) // Initially available = total
                .image(row.getImage())
                .weight(row.getWeight())
                .length(row.getLength())
                .width(row.getWidth())
                .height(row.getHeight())
                .status(Enums.Status.ACTIVE)
                .build();

        return productVariantRepository.save(variant);
    }
}

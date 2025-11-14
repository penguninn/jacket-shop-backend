package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.ProductVariantRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductVariantController {

    private final ProductVariantService productService;

    @GetMapping
    public ApiResponse<?> getAllProductVariants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("ProductVariantController::getAllProductVariants - Execution started");
        PageResponse<?> pageResponse = productService.getAllProductVariant(page, size, sortBy);
        log.info("ProductVariantController::getAllProductVariants - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all products successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getProductVariantById(@PathVariable Long id) {
        log.info("ProductVariantController::getProductVariantById - Execution started. [id: {}]", id);
        ProductVariantResponse response = productService.getProductVariantById(id);
        log.info("ProductVariantController::getProductVariantById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get product by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createProductVariant(@RequestBody ProductVariantRequest productRequest) {
        log.info("ProductVariantController::createProductVariant - Execution started.");
        ProductVariantResponse response = productService.createProductVariant(productRequest);
        log.info("ProductVariantController::createProductVariant - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("ProductVariant created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateProductVariant(@PathVariable Long id, @RequestBody ProductVariantRequest productRequest) {
        log.info("ProductVariantController::updateProductVariant - Execution started. [id: {}]", id);
        ProductVariantResponse response = productService.updateProductVariantById(productRequest, id);
        log.info("ProductVariantController::updateProductVariant - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("ProductVariant updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteProductVariant(@PathVariable Long id) {
        log.info("ProductVariantController::deleteProductVariant - Execution started. [id: {}]", id);
        productService.deleteProductVariant(id);
        log.info("ProductVariantController::deleteProductVariant - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("ProductVariant deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}

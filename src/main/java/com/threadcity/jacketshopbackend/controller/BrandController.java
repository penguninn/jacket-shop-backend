package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.BrandFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.service.BrandService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Slf4j
public class BrandController {

    private final BrandService brandService;

    @GetMapping
    public ApiResponse<?> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("BrandController::getAllBrands - Execution started");
        BrandFilterRequest request = BrandFilterRequest.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
        PageResponse<?> pageResponse = brandService.getAllBrand(request);
        log.info("BrandController::getAllBrands - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all brands successfully.")
                .data(pageResponse)
                .timestamp(java.time.Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getBrandById(@PathVariable Long id) {
        log.info("BrandController::getBrandById - Execution started. [id: {}]", id);
        BrandResponse response = brandService.getBrandById(id);
        log.info("BrandController::getBrandById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get brand by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createBrand(@RequestBody BrandRequest brandRequest) {
        log.info("BrandController::createBrand - Execution started.");
        BrandResponse response = brandService.createBrand(brandRequest);
        log.info("BrandController::createBrand - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Brand created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateBrand(@PathVariable Long id, @RequestBody BrandRequest brandRequest) {
        log.info("BrandController::updateBrand - Execution started. [id: {}]", id);
        BrandResponse response = brandService.updateBrandById(brandRequest, id);
        log.info("BrandController::updateBrand - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Brand updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteBrand(@PathVariable Long id) {
        log.info("BrandController::deleteBrand - Execution started. [id: {}]", id);
        brandService.deleteBrand(id);
        log.info("BrandController::deleteBrand - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Brand deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}

package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.*;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.filter.BrandFilterRequest;
import com.threadcity.jacketshopbackend.service.BrandService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import com.threadcity.jacketshopbackend.service.BrandImportService;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/brands")
@RequiredArgsConstructor
@Slf4j
public class BrandController {

        private final BrandService brandService;
        private final BrandImportService brandImportService;

        @PostMapping(value = "/import", consumes = "multipart/form-data")
        public ApiResponse<?> importBrands(@RequestParam("file") MultipartFile file) {
                log.info("BrandController::importBrands - Execution started");
                ImportResult result = brandImportService.importBrands(file);
                log.info("BrandController::importBrands - Execution completed. Success: {}, Error: {}", result.getSuccessCount(), result.getErrorCount());
                return ApiResponse.builder()
                        .code(200)
                        .message("Import brands completed.")
                        .data(result)
                        .timestamp(Instant.now())
                        .build();
        }

        @GetMapping
        public ApiResponse<?> getAllBrands(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("BrandController::getAllBrands - Execution started");
                BrandFilterRequest request = BrandFilterRequest.builder()
                                .status(status)
                                .search(search)
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();
                PageResponse<?> pageResponse = brandService.getAllBrands(request);
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
        public ApiResponse<?> createBrand(@Valid @RequestBody BrandRequest brandRequest) {
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
        public ApiResponse<?> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest brandRequest) {
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

        @PutMapping("/{id}/status")
        public ApiResponse<?> updateStatus(
                        @PathVariable Long id,
                        @RequestBody UpdateStatusRequest request) {
                log.info("BrandController::updateStatus - Execution started. [id: {}]", id);
                BrandResponse response = brandService.updateStatus(request, id);
                log.info("BrandController::updateStatus - Execution completed. [id: {}]", id);

                return ApiResponse.builder()
                                .code(200)
                                .message("Brand status updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/status")
        public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
                log.info("BrandController::bulkUpdateStatus - Execution started.");
                List<BrandResponse> response = brandService.bulkUpdateBrandsStatus(request);
                log.info("BrandController::bulkUpdateStatus - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk update brand status successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/delete")
        public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
                log.info("BrandController::bulkDelete - Execution started.");
                brandService.bulkDeleteBrands(request);
                log.info("BrandController::bulkDelete - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk delete brands successfully.")
                                .timestamp(Instant.now())
                                .build();
        }
}

package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.*;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
@Slf4j
public class ProductVariantController {

        private final ProductVariantService productVariantService;

        @GetMapping
        public ApiResponse<?> getAllProductVariants(
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("ProductVariantController::getAllProductVariants - Execution started");
                ProductVariantFilterRequest request = ProductVariantFilterRequest.builder()
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();
                PageResponse<?> pageResponse = productVariantService.getAllProductVariant(request);
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
                ProductVariantResponse response = productVariantService.getProductVariantById(id);
                log.info("ProductVariantController::getProductVariantById - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Get product by ID successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping
        public ApiResponse<?> createProductVariant(@Valid @RequestBody ProductVariantRequest productRequest) {
                log.info("ProductVariantController::createProductVariant - Execution started.");
                ProductVariantResponse response = productVariantService.createProductVariant(productRequest);
                log.info("ProductVariantController::createProductVariant - Execution completed.");
                return ApiResponse.builder()
                                .code(201)
                                .message("ProductVariant created successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}")
        public ApiResponse<?> updateProductVariant(@PathVariable Long id,
                        @Valid @RequestBody ProductVariantRequest productRequest) {
                log.info("ProductVariantController::updateProductVariant - Execution started. [id: {}]", id);
                ProductVariantResponse response = productVariantService.updateProductVariantById(productRequest, id);
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
                productVariantService.deleteProductVariant(id);
                log.info("ProductVariantController::deleteProductVariant - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("ProductVariant deleted successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        // UPDATE STATUS
        @PutMapping("/{id}/status")
        public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
                log.info("ProductVariantController::updateStatus - id: {}", id);
                ProductVariantResponse response = productVariantService.updateStatus(id, request.getStatus());
                return ApiResponse.builder()
                                .code(200)
                                .message("Product variant status updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        // BULK UPDATE STATUS
        @PostMapping("/bulk/status")
        public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
                log.info("ProductVariantController::bulkUpdateStatus");
                productVariantService.bulkUpdateStatus(request.getIds(), request.getStatus());
                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk update status successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        // BULK DELETE
        @PostMapping("/bulk/delete")
        public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
                log.info("ProductVariantController::bulkDelete");
                productVariantService.bulkDelete(request.getIds());
                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk delete product variants successfully.")
                                .timestamp(Instant.now())
                                .build();
        }
}

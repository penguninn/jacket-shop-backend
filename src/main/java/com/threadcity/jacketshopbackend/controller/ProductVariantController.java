package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.*;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.filter.ProductVariantFilterRequest;
import com.threadcity.jacketshopbackend.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/product-variants")
@RequiredArgsConstructor
@Slf4j
public class ProductVariantController {

        private final ProductVariantService productVariantService;

        @GetMapping
        public ApiResponse<?> getAllProductVariants(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(required = false) List<Long> colorIds,
                        @RequestParam(required = false) List<Long> sizeIds,
                        @RequestParam(required = false) List<Long> materialIds,
                        @RequestParam(required = false) BigDecimal fromPrice,
                        @RequestParam(required = false) BigDecimal toPrice,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("ProductVariantController::getAllProductVariants - Execution started");
                ProductVariantFilterRequest request = ProductVariantFilterRequest.builder()
                                .page(page)
                                .size(size)
                                .search(search)
                                .fromPrice(fromPrice)
                                .toPrice(toPrice)
                                .colorIds(colorIds)
                                .sizeIds(sizeIds)
                                .materialIds(materialIds)
                                .status(status)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();
                PageResponse<?> pageResponse = productVariantService.getAllProductVariants(request);
                log.info("ProductVariantController::getAllProductVariants - Execution completed");
                return ApiResponse.builder()
                                .code(200)
                                .message("Get all products successfully.")
                                .data(pageResponse)
                                .timestamp(Instant.now())
                                .build();
        }

        @GetMapping("/product/{productId}")
        public ApiResponse<?> getVariantsByProductId(@PathVariable Long productId) {
                log.info("Getting all variants for product: {}", productId);
                List<ProductVariantResponse> variants = productVariantService
                                .getAllProductVariantsByProductId(productId);

                return ApiResponse.builder()
                                .code(200)
                                .message("Get product variants successfully.")
                                .data(variants)
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

        @GetMapping("/sku/{sku}")
        public ApiResponse<?> getProductVariantBySku(@PathVariable String sku) {
                log.info("ProductVariantController::getProductVariantBySku - Execution started. [sku: {}]", sku);
                ProductVariantResponse response = productVariantService.getProductVariantBySku(sku);
                log.info("ProductVariantController::getProductVariantBySku - Execution completed. [sku: {}]", sku);
                return ApiResponse.builder()
                                .code(200)
                                .message("Get product by SKU successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}/stock")
        public ApiResponse<?> adjustStock(@PathVariable Long id,
                        @Valid @RequestBody StockAdjustmentRequest request) {
                log.info("ProductVariantController::adjustStock - Execution started. [id: {}]", id);
                productVariantService.adjustStock(id, request.getQuantityChange());
                log.info("ProductVariantController::adjustStock - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Product variant stock adjusted successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        @GetMapping("/check-stock")
        public ApiResponse<?> checkStock(@RequestParam Long variantId, @RequestParam Integer quantity) {
                log.info("ProductVariantController::checkStock - Execution started. [variantId: {}, quantity: {}]",
                                variantId, quantity);
                boolean available = productVariantService.isVariantAvailable(variantId, quantity);
                log.info("ProductVariantController::checkStock - Execution completed.");
                return ApiResponse.builder()
                                .code(200)
                                .message("Stock check completed.")
                                .data(available)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping
        public ApiResponse<?> createProductVariant(@Valid @RequestBody ProductVariantCreateRequest productRequest) {
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
                        @Valid @RequestBody ProductVariantUpdateRequest productRequest) {
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

        @PutMapping("/{id}/status")
        public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
                log.info("ProductVariantController::updateStatus - Execution started. [id: {}]", id);
                ProductVariantResponse response = productVariantService.updateStatus(request, id);
                log.info("ProductVariantController::updateStatus - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Product variant status updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/status")
        public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
                log.info("ProductVariantController::bulkUpdateStatus - Execution started.");

                List<ProductVariantResponse> response = productVariantService.bulkUpdateProductVariantsStatus(request);

                log.info("ProductVariantController::bulkUpdateStatus - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk update product variant status successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/delete")
        public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
                log.info("ProductVariantController::bulkDelete - Execution started.");

                productVariantService.bulkDeleteProductVariants(request);

                log.info("ProductVariantController::bulkDelete - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk delete product variants successfully.")
                                .timestamp(Instant.now())
                                .build();
        }
}

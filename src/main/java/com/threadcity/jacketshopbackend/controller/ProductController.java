package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.*;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductResponse;
import com.threadcity.jacketshopbackend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

        private final ProductService productService;

        @GetMapping
        public ApiResponse<?> getAllProducts(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(required = false) List<Long> categoryIds,
                        @RequestParam(required = false) List<Long> brandIds,
                        @RequestParam(required = false) List<Long> materialIds,
                        @RequestParam(required = false) List<Long> styleIds,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("ProductController::getAllProducts - Execution started");
                ProductFilterRequest request = ProductFilterRequest.builder()
                                .search(search)
                                .status(status)
                                .categoryIds(categoryIds)
                                .brandIds(brandIds)
                                .materialIds(materialIds)
                                .styleIds(styleIds)
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();
                PageResponse<?> pageResponse = productService.getAllProduct(request);
                log.info("ProductController::getAllProducts - Execution completed");
                return ApiResponse.builder()
                                .code(200)
                                .message("Get all products successfully.")
                                .data(pageResponse)
                                .timestamp(Instant.now())
                                .build();
        }

        @GetMapping("/{id}")
        public ApiResponse<?> getProductById(@PathVariable Long id) {
                log.info("ProductController::getProductById - Execution started. [id: {}]", id);
                ProductResponse response = productService.getProductById(id);
                log.info("ProductController::getProductById - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Get product by ID successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping
        public ApiResponse<?> createProduct(@Valid @RequestBody ProductRequest productRequest) {
                log.info("ProductController::createProduct - Execution started.");
                ProductResponse response = productService.createProduct(productRequest);
                log.info("ProductController::createProduct - Execution completed.");
                return ApiResponse.builder()
                                .code(201)
                                .message("Product created successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}")
        public ApiResponse<?> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductRequest productRequest) {
                log.info("ProductController::updateProduct - Execution started. [id: {}]", id);
                ProductResponse response = productService.updateProductById(productRequest, id);
                log.info("ProductController::updateProduct - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Product updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @DeleteMapping("/{id}")
        public ApiResponse<?> deleteProduct(@PathVariable Long id) {
                log.info("ProductController::deleteProduct - Execution started. [id: {}]", id);
                productService.deleteProduct(id);
                log.info("ProductController::deleteProduct - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Product deleted successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}/status")
        public ApiResponse<?> updateStatus(
                        @PathVariable Long id,
                        @RequestBody UpdateStatusRequest request) {
                log.info("ProductController::updateStatus - Execution started. [id: {}]", id);
                ProductResponse response = productService.updateStatus(id, request.getStatus());
                log.info("ProductController::updateStatus - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Product status updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/status")
        public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
                log.info("ProductController::bulkUpdateStatus - Execution started.");
                productService.bulkUpdateStatus(request.getIds(), request.getStatus());
                log.info("ProductController::bulkUpdateStatus - Execution completed.");
                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk update status successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/delete")
        public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
                log.info("ProductController::bulkDelete - Execution started.");
                productService.bulkDelete(request.getIds());
                log.info("ProductController::bulkDelete - Execution completed.");
                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk delete products successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

}

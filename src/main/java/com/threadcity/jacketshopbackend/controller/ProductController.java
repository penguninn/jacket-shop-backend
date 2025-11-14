package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.ProductRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ApiResponse<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("ProductController::getAllProducts - Execution started");
        PageResponse<?> pageResponse = productService.getAllProduct(page, size, sortBy);
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
    public ApiResponse<?> createProduct(@RequestBody ProductRequest productRequest) {
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
    public ApiResponse<?> updateProduct(@PathVariable Long id, @RequestBody ProductRequest productRequest) {
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
}

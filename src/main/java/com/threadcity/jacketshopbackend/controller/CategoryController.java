package com.threadcity.jacketshopbackend.controller;


import com.threadcity.jacketshopbackend.dto.request.CategoryRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.CategoryResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequestMapping("/api/categorys")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<?> getAllCategorys(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("CategoryController::getAllCategorys - Execution started");
        PageResponse<?> pageResponse = categoryService.getAllCategorys(page, size, sortBy);
        log.info("CategoryController::getAllCategorys - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all category successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getCategoryById(@PathVariable Long id) {
        log.info("CategoryController::getCategoryById - Execution started. [id: {}]", id);
        CategoryResponse response = categoryService.getCategoryById(id);
        log.info("CategoryController::getCategoryById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get category by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createCategory(@RequestBody CategoryRequest categoryRequest) {
        log.info("CategoryController::createCategory - Execution started.");
        CategoryResponse response = categoryService.createCategory(categoryRequest);
        log.info("CategoryController::createCategory - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Category created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCategory(@PathVariable Long id, @RequestBody CategoryRequest  categoryRequest) {
        log.info("CategoryController::updateCategory - Execution started. [id: {}]", id);
        CategoryResponse response = categoryService.updateCategoryById(categoryRequest, id);
        log.info("CategoryController::updateCategory - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Category updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCategory(@PathVariable Long id) {
        log.info("CategoryController::deleteCategory - Execution started. [id: {}]", id);
        categoryService.deleteCategory(id);
        log.info("CategoryController::deleteCategory - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Category deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}

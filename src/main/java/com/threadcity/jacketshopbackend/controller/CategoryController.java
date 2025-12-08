package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.filter.CategoryFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.CategoryRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.CategoryResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class CategoryController {
        private final CategoryService categoryService;

        @GetMapping
        public ApiResponse<?> getAllCategories(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("CategoryController::getAllCategories - Execution started");

                CategoryFilterRequest request = CategoryFilterRequest.builder()
                                .search(search)
                                .status(status)
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();

                PageResponse<?> pageResponse = categoryService.getAllCategories(request);

                log.info("CategoryController::getAllCategories - Execution completed");

                return ApiResponse.builder()
                                .code(200)
                                .message("Get all categories successfully.")
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
        public ApiResponse<?> createCategory(@Valid @RequestBody CategoryRequest categoryRequest) {
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
        public ApiResponse<?> updateCategory(@PathVariable Long id,
                        @Valid @RequestBody CategoryRequest categoryRequest) {
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

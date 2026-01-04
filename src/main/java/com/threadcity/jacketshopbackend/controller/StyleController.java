package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.product.request.StyleCreateRequest;
import com.threadcity.jacketshopbackend.dto.product.request.StyleUpdateRequest;
import com.threadcity.jacketshopbackend.dto.common.request.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.common.request.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.common.request.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.common.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.common.response.ImportResult;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.product.response.StyleResponse;
import com.threadcity.jacketshopbackend.filter.StyleFilterRequest;
import com.threadcity.jacketshopbackend.service.StyleImportService;
import com.threadcity.jacketshopbackend.service.StyleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/styles")
@RequiredArgsConstructor
@Slf4j
public class StyleController {

        private final StyleService styleService;
        private final StyleImportService styleImportService;

        @PostMapping(value = "/import", consumes = "multipart/form-data")
        public ApiResponse<?> importStyles(@RequestParam("file") MultipartFile file) {
                log.info("StyleController::importStyles - Execution started");
                ImportResult result = styleImportService.importStyles(file);
                log.info("StyleController::importStyles - Execution completed. Success: {}, Error: {}", result.getSuccessCount(), result.getErrorCount());
                return ApiResponse.builder()
                        .code(200)
                        .message("Import styles completed.")
                        .data(result)
                        .timestamp(Instant.now())
                        .build();
        }

        @GetMapping
        public ApiResponse<?> getAllStyles(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("StyleController::getAllStyles - Execution started");
                StyleFilterRequest request = StyleFilterRequest.builder()
                                .status(status)
                                .search(search)
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();
                PageResponse<?> pageResponse = styleService.getAllStyles(request);
                log.info("StyleController::getAllStyles - Execution completed");
                return ApiResponse.builder()
                                .code(200)
                                .message("Get all styles successfully.")
                                .data(pageResponse)
                                .timestamp(Instant.now())
                                .build();
        }

        @GetMapping("/{id}")
        public ApiResponse<?> getStyleById(@PathVariable Long id) {
                log.info("StyleController::getStyleById - Execution started. [id: {}]", id);
                StyleResponse response = styleService.getStyleById(id);
                log.info("StyleController::getStyleById - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Get style by ID successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping
        public ApiResponse<?> createStyle(@Valid @RequestBody StyleCreateRequest styleRequest) {
                log.info("StyleController::createStyle - Execution started.");
                StyleResponse response = styleService.createStyle(styleRequest);
                log.info("StyleController::createStyle - Execution completed.");
                return ApiResponse.builder()
                                .code(201)
                                .message("Style created successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}")
        public ApiResponse<?> updateStyle(@PathVariable Long id, @Valid @RequestBody StyleCreateRequest styleRequest) {
                log.info("StyleController::updateStyle - Execution started. [id: {}]", id);
                StyleResponse response = styleService.updateStyleById(styleRequest, id);
                log.info("StyleController::updateStyle - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Style updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @DeleteMapping("/{id}")
        public ApiResponse<?> deleteStyle(@PathVariable Long id) {
                log.info("StyleController::deleteStyle - Execution started. [id: {}]", id);
                styleService.deleteStyle(id);
                log.info("StyleController::deleteStyle - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Style deleted successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}/status")
        public ApiResponse<?> updateStatus(
                        @PathVariable Long id,
                        @RequestBody UpdateStatusRequest request) {
                log.info("StyleController::updateStatus - Execution started. [id: {}]", id);
                StyleResponse response = styleService.updateStatus(request, id);
                log.info("StyleController::updateStatus - Execution completed. [id: {}]", id);

                return ApiResponse.builder()
                                .code(200)
                                .message("Style status updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/status")
        public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
                log.info("StyleController::bulkUpdateStatus - Execution started.");
                List<StyleResponse> response = styleService.bulkUpdateStylesStatus(request);
                log.info("StyleController::bulkUpdateStatus - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk update style status successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/delete")
        public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
                log.info("StyleController::bulkDelete - Execution started.");
                styleService.bulkDeleteStyles(request);
                log.info("StyleController::bulkDelete - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk delete styles successfully.")
                                .timestamp(Instant.now())
                                .build();
        }
}

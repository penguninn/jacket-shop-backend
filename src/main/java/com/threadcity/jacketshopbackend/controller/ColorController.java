package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.filter.ColorFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.ColorResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.service.ColorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

import com.threadcity.jacketshopbackend.service.ColorImportService;
import com.threadcity.jacketshopbackend.dto.response.ImportResult;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
@Slf4j
public class ColorController {

        private final ColorService colorService;
        private final ColorImportService colorImportService;

        @PostMapping(value = "/import", consumes = "multipart/form-data")
        public ApiResponse<?> importColors(@RequestParam("file") MultipartFile file) {
                log.info("ColorController::importColors - Execution started");
                ImportResult result = colorImportService.importColors(file);
                log.info("ColorController::importColors - Execution completed. Success: {}, Error: {}", result.getSuccessCount(), result.getErrorCount());
                return ApiResponse.builder()
                        .code(200)
                        .message("Import colors completed.")
                        .data(result)
                        .timestamp(Instant.now())
                        .build();
        }

        @GetMapping
        public ApiResponse<?> getAllColors(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("ColorController::getAllColors - Execution started");

                ColorFilterRequest request = ColorFilterRequest.builder()
                                .search(search)
                                .status(status)
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();

                PageResponse<?> pageResponse = colorService.getAllColors(request);

                log.info("ColorController::getAllColors - Execution completed");

                return ApiResponse.builder()
                                .code(200)
                                .message("Get all colors successfully.")
                                .data(pageResponse)
                                .timestamp(Instant.now())
                                .build();
        }

        @GetMapping("/{id}")
        public ApiResponse<?> getColorById(@PathVariable Long id) {
                log.info("ColorController::getColorById - Execution started. [id: {}]", id);
                ColorResponse response = colorService.getColorById(id);
                log.info("ColorController::getColorById - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Get color by ID successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping
        public ApiResponse<?> createColor(@Valid @RequestBody ColorRequest colorRequest) {
                log.info("ColorController::createColor - Execution started.");
                ColorResponse response = colorService.createColor(colorRequest);
                log.info("ColorController::createColor - Execution completed.");
                return ApiResponse.builder()
                                .code(201)
                                .message("Color created successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}")
        public ApiResponse<?> updateColor(@PathVariable Long id, @Valid @RequestBody ColorRequest colorRequest) {
                log.info("ColorController::updateColor - Execution started. [id: {}]", id);
                ColorResponse response = colorService.updateColorById(colorRequest, id);
                log.info("ColorController::updateColor - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Color updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}/status")
        public ApiResponse<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request) {
                log.info("ColorController::updateStatus - Execution started. [id: {}]", id);
                ColorResponse response = colorService.updateStatus(request, id);
                log.info("ColorController::updateStatus - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Color status updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @DeleteMapping("/{id}")
        public ApiResponse<?> deleteColor(@PathVariable Long id) {
                log.info("ColorController::deleteColor - Execution started. [id: {}]", id);
                colorService.deleteColor(id);
                log.info("ColorController::deleteColor - Execution completed. [id: {}]", id);
                return ApiResponse.builder()
                                .code(200)
                                .message("Color deleted successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/status")
        public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
                log.info("ColorController::bulkUpdateStatus - Execution started.");
                List<ColorResponse> responses = colorService.bulkUpdateStatus(request);
                log.info("ColorController::bulkUpdateStatus - Execution completed.");
                return ApiResponse.builder()
                                .code(200)
                                .data(responses)
                                .message("Colors statuses updated successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/delete")
        public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
                log.info("ColorController::bulkDelete - Execution started.");
                colorService.bulkDelete(request);
                log.info("ColorController::bulkDelete - Execution completed.");
                return ApiResponse.builder()
                                .code(200)
                                .message("Colors deleted successfully.")
                                .timestamp(Instant.now())
                                .build();
        }
}

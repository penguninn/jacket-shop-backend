package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.filter.ColorFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
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

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
@Slf4j
public class ColorController {

        private final ColorService colorService;

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
}

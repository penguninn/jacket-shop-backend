package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.StyleRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.StyleResponse;
import com.threadcity.jacketshopbackend.service.StyleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/styles")
@RequiredArgsConstructor
@Slf4j
public class StyleController {

    private final StyleService styleService;

    @GetMapping
    public ApiResponse<?> getAllStyles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdDate,desc") String sortBy
    ) {
        log.info("StyleController::getAllStyles - Execution started");
        PageResponse<?> pageResponse = styleService.getAllStyle(page, size, sortBy);
        log.info("StyleController::getAllStyles - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all styles successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getStyleById(@PathVariable Integer id) {
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
    public ApiResponse<?> createStyle(@RequestBody StyleRequest styleRequest) {
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
    public ApiResponse<?> updateStyle(@PathVariable Integer id, @RequestBody StyleRequest styleRequest) {
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
    public ApiResponse<?> deleteStyle(@PathVariable Integer id) {
        log.info("StyleController::deleteStyle - Execution started. [id: {}]", id);
        styleService.deleteStyle(id);
        log.info("StyleController::deleteStyle - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Style deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}

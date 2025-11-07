package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.dto.response.ColorResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.service.BrandService;
import com.threadcity.jacketshopbackend.service.ColorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/colors")
@RequiredArgsConstructor
@Slf4j
public class ColorController {

    private final ColorService colorService;

    @GetMapping
    public ApiResponse<?> getAllColor(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("ColorController::getAllColor - Execution started");
        PageResponse<?> pageResponse = colorService.getAllColor(page, size, sortBy);
        log.info("ColorController::getAllColor - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all color successfully.")
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
    public ApiResponse<?> createColor(@RequestBody ColorRequest colorRequest) {
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
    public ApiResponse<?> updateColor(@PathVariable Long id, @RequestBody ColorRequest colorRequest) {
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



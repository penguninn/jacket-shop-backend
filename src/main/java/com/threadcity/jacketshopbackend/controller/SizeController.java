package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import com.threadcity.jacketshopbackend.service.BrandService;
import com.threadcity.jacketshopbackend.service.SizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
@Slf4j
public class SizeController {
    private final SizeService sizeService;

    @GetMapping
    public ApiResponse<?> getAllSize(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("SizeController::getAllSize - Execution started");
        PageResponse<?> pageResponse = sizeService.getAllSize(page, size, sortBy);
        log.info("SizeController::getAllSize - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all size successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getSizeByID(@PathVariable Long id) {
        log.info("SizeController::getSizeById - Execution started. [id: {}]", id);
        SizeResponse response = sizeService.getSizeById(id);
        log.info("SizeController::getSizeById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get size by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createSize(@RequestBody SizeRequest sizeRequest) {
        log.info("SizeController::createSize - Execution started.");
        SizeResponse response = sizeService.createSize(sizeRequest);
        log.info("SizeController::createSize - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Size created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateSize(@PathVariable Long id, @RequestBody SizeRequest sizeRequest) {
        log.info("SizeController::updateSize - Execution started. [id: {}]", id);
        SizeResponse response = sizeService.updateSizeById(sizeRequest, id);
        log.info("SizeController::updateSize - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Brand updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteSize(@PathVariable Long id) {
        log.info("SizeController::deleteSize - Execution started. [id: {}]", id);
        sizeService.deleteSize(id);
        log.info("SizeController::deleteSize - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Size deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}

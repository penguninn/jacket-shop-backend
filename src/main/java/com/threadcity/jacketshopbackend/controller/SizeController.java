package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.SizeFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import com.threadcity.jacketshopbackend.service.SizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/sizes")
@RequiredArgsConstructor
@Slf4j
public class SizeController {
        private final SizeService sizeService;

        @GetMapping
        public ApiResponse<?> getAllSizes(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("SizeController::getAllSizes - Execution started");

                SizeFilterRequest request = SizeFilterRequest.builder()
                                .search(search)
                                .status(status)
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();

                PageResponse<?> pageResponse = sizeService.getAllSizes(request);

                log.info("SizeController::getAllSizes - Execution completed");

                return ApiResponse.builder()
                                .code(200)
                                .message("Get all sizes successfully.")
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

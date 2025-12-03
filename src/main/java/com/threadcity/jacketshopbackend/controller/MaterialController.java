package com.threadcity.jacketshopbackend.controller;
import com.threadcity.jacketshopbackend.dto.request.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.MaterialFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.MaterialRequest;
import com.threadcity.jacketshopbackend.dto.request.MaterialStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.StyleRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.MaterialResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.StyleResponse;
import com.threadcity.jacketshopbackend.service.MaterialService;
import jakarta.validation.Valid;
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
import java.util.List;

@RestController
@RequestMapping("/api/materials")
@RequiredArgsConstructor
@Slf4j
public class MaterialController {
    private final MaterialService materialService;

    @GetMapping
    public ApiResponse<?> getAllMaterials(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info("MaterialController::getAllMaterials - Execution started");

        MaterialFilterRequest request = MaterialFilterRequest.builder()
                .search(search)
                .status(status)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<?> pageResponse = materialService.getAllMaterials(request);

        log.info("MaterialController::getAllMaterials - Execution completed");

        return ApiResponse.builder()
                .code(200)
                .message("Get all materials successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getMaterialById(@PathVariable Long id) {
        log.info("MaterialController::getMaterialById - Execution started. [id: {}]", id);
        MaterialResponse response = materialService.getMaterialById(id);
        log.info("MaterialController::getMaterialById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get material by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createMaterial(@RequestBody MaterialRequest materialRequest) {
        log.info("MaterialController::createMaterial - Execution started.");
        MaterialResponse response = materialService.createMaterial(materialRequest);
        log.info("MaterialController::createMaterial - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Material created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateMaterial(@PathVariable Long id,
                                         @Valid @RequestBody MaterialRequest materialRequest) {
        log.info("MaterialController::updateMaterial - Execution started. [id: {}]", id);

        MaterialResponse response = materialService.updateMaterialById(materialRequest, id);

        log.info("MaterialController::updateMaterial - Execution completed. [id: {}]", id);

        return ApiResponse.builder()
                .code(200)
                .message("Material updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }


    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteMaterial(@PathVariable Long id) {
        log.info("MaterialController::deleteMaterial - Execution started. [id: {}]", id);
        materialService.deleteMaterial(id);
        log.info("MaterialController::deleteMaterial - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Material deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateMaterialStatus(
            @PathVariable Long id,
            @RequestBody MaterialStatusRequest request
    ) {
        log.info("MaterialController::updateMaterialStatus - Execution started. [id: {}]", id);

        MaterialResponse response = materialService.updateMaterialStatus(id, request.getStatus());

        log.info("MaterialController::updateMaterialStatus - Execution completed. [id: {}]", id);

        return ApiResponse.builder()
                .code(200)
                .message("Material status updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // =============================
    // BULK UPDATE STATUS
    // =============================
    @PostMapping("/bulk/status")
    public ApiResponse<?> bulkUpdateStatus(@RequestBody BulkStatusRequest request) {
        log.info("MaterialController::bulkUpdateStatus - Execution started.");

        materialService.bulkUpdateStatus(request.getIds(), request.getStatus());

        log.info("MaterialController::bulkUpdateStatus - Execution completed.");

        return ApiResponse.builder()
                .code(200)
                .message("Bulk update material status successfully.")
                .timestamp(Instant.now())
                .build();
    }

    // =============================
    // BULK DELETE
    // =============================
    @PostMapping("/bulk/delete")
    public ApiResponse<?> bulkDelete(@RequestBody BulkDeleteRequest request) {
        log.info("MaterialController::bulkDelete - Execution started.");

        materialService.bulkDelete(request.getIds());

        log.info("MaterialController::bulkDelete - Execution completed.");

        return ApiResponse.builder()
                .code(200)
                .message("Bulk delete materials successfully.")
                .timestamp(Instant.now())
                .build();
    }
}


package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.CouponResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.request.CouponRequest;
import com.threadcity.jacketshopbackend.dto.request.CouponFilterRequest;
import com.threadcity.jacketshopbackend.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ApiResponse<?> getAllCoupons(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) List<String> status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir,
            @RequestParam(required = false) List<String> type

            ) {
        log.info("CouponController::getAllCoupons - Execution started");
        CouponFilterRequest request = CouponFilterRequest.builder()
                .search(search)
                .type(type)
                .status(status)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();
        PageResponse<?> pageResponse = couponService.getAllCoupons(request);
        log.info("CouponController::getAllCoupons - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all coupons successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getCouponById(@PathVariable Long id) {
        log.info("CouponController::getCouponById - Execution started. [id: {}]", id);
        CouponResponse response = couponService.getCouponById(id);
        log.info("CouponController::getCouponById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get coupon by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createCoupon(@Valid @RequestBody CouponRequest request) {
        log.info("CouponController::createCoupon - Execution started");
        CouponResponse response = couponService.createCoupon(request);
        log.info("CouponController::createCoupon - Execution completed");
        return ApiResponse.builder()
                .code(201)
                .message("Coupon created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCoupon(@PathVariable Long id, @Valid @RequestBody CouponRequest request) {
        log.info("CouponController::updateCoupon - Execution started. [id: {}]", id);
        CouponResponse response = couponService.updateCouponById(id, request);
        log.info("CouponController::updateCoupon - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Coupon updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteCoupon(@PathVariable Long id) {
        log.info("CouponController::deleteCoupon - Execution started. [id: {}]", id);
        couponService.deleteCoupon(id);
        log.info("CouponController::deleteCoupon - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Coupon deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/status")
    public ApiResponse<?> updateStatus(@PathVariable Long id, @RequestBody UpdateStatusRequest request) {
        log.info("CouponController::updateStatus - Execution started. [id: {}]", id);
        CouponResponse response = couponService.updateStatus(id, request.getStatus());
        log.info("CouponController::updateStatus - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Coupon status updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/bulk/status")
    public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
        log.info("CouponController::bulkUpdateStatus - Execution started");
        couponService.bulkUpdateStatus(request.getIds(), request.getStatus());
        log.info("CouponController::bulkUpdateStatus - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Bulk update status successfully.")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/bulk/delete")
    public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
        log.info("CouponController::bulkDelete - Execution started");
        couponService.bulkDelete(request.getIds());
        log.info("CouponController::bulkDelete - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Bulk delete coupons successfully.")
                .timestamp(Instant.now())
                .build();
    }

}

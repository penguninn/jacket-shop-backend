package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.CouponRequest;
import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.CouponResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import com.threadcity.jacketshopbackend.service.CouponService;
import com.threadcity.jacketshopbackend.service.SizeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Slf4j
public class CouponController {
    private final CouponService couponService;

    @GetMapping
    public ApiResponse<?> getAllCoupon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("CouponController::getAllCoupon - Execution started");
        PageResponse<?> pageResponse = couponService.getAllCoupon(page, size, sortBy);
        log.info("CouponController::getAllCoupon - Execution completed");
        return ApiResponse.builder()
                .code(200)
                .message("Get all coupon successfully.")
                .data(pageResponse)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    public ApiResponse<?> getCouponByID(@PathVariable Long id) {
        log.info("CouponController::getCouponByID - Execution started. [id: {}]", id);
        CouponResponse response = couponService.getCouponById(id);
        log.info("CouponController::getCouponByID - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get coupon by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping
    public ApiResponse<?> createCoupon(@RequestBody CouponRequest couponRequest) {
        log.info("CouponController::createCoupon - Execution started.");
        CouponResponse response = couponService.createCoupon(couponRequest);
        log.info("CouponController::createCoupon - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Coupon created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}")
    public ApiResponse<?> updateCoupon(@PathVariable Long id, @RequestBody CouponRequest couponRequest) {
        log.info("CouponController::updateCoupon - Execution started. [id: {}]", id);
        CouponResponse response = couponService.updateCouponById(couponRequest, id);
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
}



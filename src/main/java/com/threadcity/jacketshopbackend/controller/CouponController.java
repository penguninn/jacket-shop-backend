package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.CouponFilterRequest;
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
            @RequestParam(required = false) List<String> type,
            @RequestParam(required = false) Instant validFrom,
            @RequestParam(required = false) Instant validTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        log.info("CouponController::getAllCoupons - Execution started");

        CouponFilterRequest request = CouponFilterRequest.builder()
                .search(search)
                .status(status)
                .type(type)
                .validFrom(validFrom)
                .validTo(validTo)
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



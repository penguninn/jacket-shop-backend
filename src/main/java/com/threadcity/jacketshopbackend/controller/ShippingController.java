package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.goship.rate.GoshipRateData;
import com.threadcity.jacketshopbackend.dto.goship.rate.GoshipRateRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.service.ShippingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
@Slf4j
public class ShippingController {

    private final ShippingService shippingService;

    @PostMapping("/rates")
    public ApiResponse<List<GoshipRateData>> getRates(@Valid @RequestBody GoshipRateRequest request) {
        log.info("ShippingController::getRates - Execution started.");
        List<GoshipRateData> rates = shippingService.getRates(request);
        log.info("ShippingController::getRates - Execution completed.");
        return ApiResponse.<List<GoshipRateData>>builder()
                .code(200)
                .message("Shipping rates fetched successfully.")
                .data(rates)
                .timestamp(Instant.now())
                .build();
    }
}

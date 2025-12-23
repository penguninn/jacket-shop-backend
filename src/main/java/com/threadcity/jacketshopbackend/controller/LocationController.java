package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.DistrictResponse;
import com.threadcity.jacketshopbackend.dto.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.dto.response.WardResponse;
import com.threadcity.jacketshopbackend.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.threadcity.jacketshopbackend.service.LocationService;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/location")
@Slf4j
public class LocationController {

    private final AddressService addressService;
    private final LocationService locationService;

    @GetMapping("/provinces")
    public ApiResponse<?> getAllProvinces() {
        log.info("LocationController::getAllProvinces - Execution started");
        List<ProvinceResponse> provinceResponses = addressService.getAllProvinces();
        log.info("LocationController::getAllProvinces - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(provinceResponses)
                .message("Get all provinces successfully")
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/districts")
    public ApiResponse<?> getAllDistricts(@RequestParam(required = true) Long provinceId) {
        log.info("LocationController::getAllDistricts - Execution started");
        List<DistrictResponse> districtResponses = addressService.getAllDistricts(provinceId);
        log.info("LocationController::getAllDistricts - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(districtResponses)
                .message("Get all districts successfully")
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/wards")
    public ApiResponse<?> getAllWards(@RequestParam(required = true) Long districtsId) {
        log.info("LocationController::getAllWards - Execution started");
        List<WardResponse> wardResponses = addressService.getAllWards(districtsId);
        log.info("LocationController::getAllWards - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(wardResponses)
                .message("Get all wards successfully")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/sync")
    public ApiResponse<?> syncAddressData() {
        log.info("LocationController::syncAddressData - Execution started");
        locationService.syncAllAddressData();
        log.info("LocationController::syncAddressData - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .message("Sync address data successfully")
                .timestamp(Instant.now())
                .build();
    }

}
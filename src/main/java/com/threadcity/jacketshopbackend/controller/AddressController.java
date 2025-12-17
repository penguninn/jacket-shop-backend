package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.DistrictResponse;
import com.threadcity.jacketshopbackend.dto.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.dto.response.WardResponse;
import com.threadcity.jacketshopbackend.service.AddressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import com.threadcity.jacketshopbackend.service.AddressSyncService;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
@Slf4j
public class AddressController {

    private final AddressService addressService;
    private final AddressSyncService addressSyncService;

    @GetMapping("/provinces")
    public ApiResponse<?> getAllProvinces() {
        log.info("AddressController::getAllProvinces - Execution started");
        List<ProvinceResponse> provinceResponses = addressService.getAllProvinces();
        log.info("AddressController::getAllProvinces - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(provinceResponses)
                .message("Get all provinces successfull")
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/districts")
    public ApiResponse<?> getAllDistricts(@RequestParam(required = true) Long provinceId) {
        log.info("AddressController::getAllProvinces - Execution started");
        List<DistrictResponse> districtResponses = addressService.getAllDistricts(provinceId);
        log.info("AddressController::getAllDistricts - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(districtResponses)
                .message("Get all districts successfull")
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/wards")
    public ApiResponse<?> getAllWards(@RequestParam(required = true) Long districtsId) {
        log.info("AddressController::getAllWards - Execution started");
        List<WardResponse> wardResponses = addressService.getAllWards(districtsId);
        log.info("AddressController::getAllWards - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .data(wardResponses)
                .message("Get all wards successfull")
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/sync")
    public ApiResponse<?> syncAddressData() {
        log.info("AddressController::syncAddressData - Execution started");
        addressSyncService.syncAllAddressData();
        log.info("AddressController::syncAddressData - Execution ended");
        return ApiResponse.builder()
                .code(200)
                .message("Sync address data successfull")
                .timestamp(Instant.now())
                .build();
    }

}

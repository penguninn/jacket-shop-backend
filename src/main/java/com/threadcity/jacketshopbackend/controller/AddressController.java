package com.threadcity.jacketshopbackend.controller;

import java.time.Instant;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.DistrictResponse;
import com.threadcity.jacketshopbackend.dto.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.dto.response.WardResponse;
import com.threadcity.jacketshopbackend.service.AddressService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/address")
@Slf4j
public class AddressController {

    private final AddressService addressService;

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

}

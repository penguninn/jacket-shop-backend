package com.threadcity.jacketshopbackend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.dto.response.DistrictResponse;
import com.threadcity.jacketshopbackend.dto.response.ProvinceResponse;
import com.threadcity.jacketshopbackend.dto.response.WardResponse;
import com.threadcity.jacketshopbackend.mapper.DistrictMapper;
import com.threadcity.jacketshopbackend.mapper.ProvinceMapper;
import com.threadcity.jacketshopbackend.mapper.WardMapper;
import com.threadcity.jacketshopbackend.repository.DistrictRepository;
import com.threadcity.jacketshopbackend.repository.ProvinceRepository;
import com.threadcity.jacketshopbackend.repository.WardRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final ProvinceMapper provinceMapper;
    private final DistrictMapper districtMapper;
    private final WardMapper wardMapper;

    public List<ProvinceResponse> getAllProvinces() {
        log.info("AddressService::getAllProvinces - Execution started");
        List<ProvinceResponse> provinceResponses = provinceRepository.findAll().stream()
                .map(provinceMapper::toDto)
                .toList();
        log.info("AddressService::getAllProvinces - Execution ended");
        return provinceResponses;
    }

    public List<DistrictResponse> getAllDistricts(Long provinceId) {
        log.info("AddressService::getAllDistricts - Execution started");
        List<DistrictResponse> districtResponses = districtRepository.findAllByProvinceId(provinceId)
                .stream()
                .map(districtMapper::toDto)
                .toList();
        log.info("AddressService::getAllDistricts - Execution ended");
        return districtResponses;
    }

    public List<WardResponse> getAllWards(Long districtId) {
        log.info("AddressService::getAllWards - Execution started");
        List<WardResponse> wardResponses = wardRepository.findAllByDistrictId(districtId)
                .stream()
                .map(wardMapper::toDto)
                .toList();
        log.info("AddressService::getAllWards - Execution ended");
        return wardResponses;
    }
}

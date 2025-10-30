package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.BrandMapper;
import com.threadcity.jacketshopbackend.repository.BrandRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrandService {
    private final BrandRepository brandRepository;
    private final BrandMapper brandMapper;

    public BrandResponse getBrandById(Long Id) {
        log.info("BrandService::getBrandById - Execution started. [Id: {}]", Id);
        Brand brand = brandRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Brand not found with BrandId: " + Id));
        log.info("BrandService::getBrandById - Execution completed. [BrandId: {}]", Id);
        return brandMapper.toDto(brand);
    }

    public PageResponse<?> getAllBrand(int page, int size, String sortBy) {
        log.info("BrandService::getAllBrand - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Brand> brandPage = brandRepository.findAll(pageable);
            List<BrandResponse> BrandList = brandPage.stream()
                    .map(brandMapper::toDto)
                    .toList();
            log.info("BrandService::getAllBrand - Execution completed.");
            return PageResponse.builder()
                    .contents(BrandList)
                    .size(size)
                    .page(p)
                    .totalPages(brandPage.getTotalPages())
                    .totalElements(brandPage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("BrandService::getAllBrand - Execution failed.", e);
            throw new BusinessException("BrandService::getAllBrand - Execution failed.");
        }
    }
    @Transactional
    public BrandResponse createBrand(BrandRequest brand) {
        log.info("BrandService::createBrand - Execution started.");
        if (brandRepository.existsByName(brand.getName())) {
            throw new BusinessException("Brand already exists with name: " + brand.getName());
        }
        try {
            Brand brandEntity = brandMapper.toEntity(brand);
            Brand savedBrand = brandRepository.save(brandEntity);
            log.info("BrandService::createBrand - Execution completed.");
            return brandMapper.toDto(savedBrand);
        } catch (Exception e) {
            log.error("BrandService::createBrand - Execution failed.", e);
            throw new BusinessException("BrandService::createBrand - Execution failed.");
        }
    }
    @Transactional
    public BrandResponse updateBrandById(BrandRequest brandRequest, Long id) {
        log.info("BrandService::updateBrandById - Execution started.");
        try {
            Brand brand = brandRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Brand not found with BrandId: " + id));
            brand.setName(brandRequest.getName());
            brand.setLogoUrl(brandRequest.getLogoUrl());
            brand.setStatus(brandRequest.getStatus());
            Brand savedBrand = brandRepository.save(brand);
            log.info("BrandService::updateProfile - Execution completed. [BrandId: {}]", id);
            return brandMapper.toDto(savedBrand);
        } catch (RuntimeException e) {
            log.error("BrandService::updateProfile - Execution failed.", e);
            throw new BusinessException("BrandService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteBrand(Long id) {
        log.info("BrandService::deleteBrand - Execution started.");
        try {
            if (!brandRepository.existsById(id)) {
                throw new EntityNotFoundException("Brand not found with BrandId: " + id);
            }
            brandRepository.deleteById(id);
            log.info("BrandService::deleteBrand - Execution completed.");
        } catch (Exception e) {
            log.error("BrandService::deleteBrand - Execution failed.", e);
            throw new BusinessException("BrandService::deleteBrand - Execution failed.");
        }
    }
}

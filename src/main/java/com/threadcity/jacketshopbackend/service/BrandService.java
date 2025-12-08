package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.BrandFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.BrandMapper;
import com.threadcity.jacketshopbackend.repository.BrandRepository;
import com.threadcity.jacketshopbackend.specification.BrandSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                        "Brand not found with id: " + Id));
        log.info("BrandService::getBrandById - Execution completed. [BrandId: {}]", Id);
        return brandMapper.toDto(brand);
    }

    public PageResponse<?> getAllBrand(BrandFilterRequest request) {
        log.info("BrandService::getAllBrand - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Brand> spec = BrandSpecification.buildSpec(request);
        Page<Brand> brandPage = brandRepository.findAll(spec, pageable);

        List<BrandResponse> brandList = brandPage.getContent().stream()
                .map(brandMapper::toDto)
                .toList();

        log.info("BrandService::getAllBrand - Execution completed.");
        return PageResponse.builder()
                .contents(brandList)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(brandPage.getTotalPages())
                .totalElements(brandPage.getTotalElements())
                .build();
    }

    @Transactional
    public BrandResponse createBrand(BrandRequest brand) {
        log.info("BrandService::createBrand - Execution started.");
        if (brandRepository.existsByName(brand.getName())) {
            throw new ResourceConflictException(ErrorCodes.BRAND_NAME_DUPLICATE,
                    "Brand already exists with name: " + brand.getName());
        }

        Brand brandEntity = brandMapper.toEntity(brand);
        Brand savedBrand = brandRepository.save(brandEntity);
        log.info("BrandService::createBrand - Execution completed.");
        return brandMapper.toDto(savedBrand);
    }

    @Transactional
    public BrandResponse updateBrandById(BrandRequest brandRequest, Long id) {
        log.info("BrandService::updateBrandById - Execution started.");

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                        "Brand not found with id: " + id));

        if (!brand.getName().equals(brandRequest.getName()) && brandRepository.existsByName(brandRequest.getName())) {
            throw new ResourceConflictException(ErrorCodes.BRAND_NAME_DUPLICATE,
                    "Brand already exists with name: " + brandRequest.getName());
        }

        brand.setName(brandRequest.getName());
        brand.setLogoUrl(brandRequest.getLogoUrl());
        brand.setStatus(brandRequest.getStatus());
        brand.setDescription(brandRequest.getDescription());
        Brand savedBrand = brandRepository.save(brand);
        log.info("BrandService::updateProfile - Execution completed. [BrandId: {}]", id);
        return brandMapper.toDto(savedBrand);
    }

    @Transactional
    public void deleteBrand(Long id) {
        log.info("BrandService::deleteBrand - Execution started.");

        if (!brandRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND, "Brand not found with id: " + id);
        }
        brandRepository.deleteById(id);
        log.info("BrandService::deleteBrand - Execution completed.");
    }

    @Transactional
    public BrandResponse updateStatus(Long id, String status) {
        log.info("BrandService::updateStatus - Execution started. [id: {}]", id);

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                        "Brand not found with id: " + id));

        brand.setStatus(Enum.valueOf(com.threadcity.jacketshopbackend.common.Enums.Status.class, status.toUpperCase()));

        Brand saved = brandRepository.save(brand);

        log.info("BrandService::updateStatus - Execution completed. [id: {}]", id);

        return brandMapper.toDto(saved);
    }

    // =============================
    // BULK UPDATE STATUS
    // =============================
    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        log.info("BrandService::bulkUpdateStatus - Execution started.");

        List<Brand> brands = brandRepository.findAllById(ids);
        brands.forEach(b -> b.setStatus(
                Enum.valueOf(com.threadcity.jacketshopbackend.common.Enums.Status.class, status.toUpperCase())));

        brandRepository.saveAll(brands);

        log.info("BrandService::bulkUpdateStatus - Execution completed.");
    }

    // =============================
    // BULK DELETE
    // =============================
    @Transactional
    public void bulkDelete(List<Long> ids) {
        log.info("BrandService::bulkDelete - Execution started.");

        List<Brand> brands = brandRepository.findAllById(ids);

        if (brands.size() != ids.size()) {
            // Calculate missing IDs
            List<Long> foundIds = brands.stream().map(Brand::getId).toList();
            List<Long> missingIds = ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND, "Brands not found: " + missingIds);
        }

        brandRepository.deleteAllInBatch(brands);

        log.info("BrandService::bulkDelete - Execution completed.");
    }
}

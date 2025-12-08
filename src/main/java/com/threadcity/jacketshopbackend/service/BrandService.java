package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.filter.BrandFilterRequest;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

    public PageResponse<?> getAllBrands(BrandFilterRequest request) {
        log.info("BrandService::getAllBrands - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Brand> spec = BrandSpecification.buildSpec(request);
        Page<Brand> brandPage = brandRepository.findAll(spec, pageable);

        List<BrandResponse> brandList = brandPage.getContent().stream()
                .map(brandMapper::toDto)
                .toList();

        log.info("BrandService::getAllBrands - Execution completed.");
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

        if (brandRepository.existsByNameAndIdNot(brandRequest.getName(), id)) {
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
    public BrandResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("BrandService::updateStatus - Execution started.");

        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                        "Brand not found with id: " + id));

        brand.setStatus(request.getStatus());

        Brand saved = brandRepository.save(brand);

        log.info("BrandService::updateStatus - Execution completed. [id: {}]", id);

        return brandMapper.toDto(saved);
    }

    @Transactional
    public List<BrandResponse> bulkUpdateBrandsStatus(BulkStatusRequest request) {
        log.info("BrandService::bulkUpdateBrandsStatus - Execution started.");

        List<Brand> brands = brandRepository.findAllById(request.getIds());
        if (brands.size() != request.getIds().size()) {
            Set<Long> foundIds = brands.stream().map(Brand::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND, "Brands not found: " + missingIds);
        }

        brands.forEach(b -> b.setStatus(request.getStatus()));

        List<Brand> savedBrands = brandRepository.saveAll(brands);

        log.info("BrandService::bulkUpdateBrandsStatus - Execution completed.");
        return savedBrands.stream().map(brandMapper::toDto).toList();
    }

    @Transactional
    public void bulkDeleteBrands(BulkDeleteRequest request) {
        log.info("BrandService::bulkDeleteBrands - Execution started.");

        List<Brand> brands = brandRepository.findAllById(request.getIds());

        if (brands.size() != request.getIds().size()) {
            Set<Long> foundIds = brands.stream().map(Brand::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND, "Brands not found: " + missingIds);
        }

        brandRepository.deleteAllInBatch(brands);

        log.info("BrandService::bulkDeleteBrands - Execution completed.");
    }
}

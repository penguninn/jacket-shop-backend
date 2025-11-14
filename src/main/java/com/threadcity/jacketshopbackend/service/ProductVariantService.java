package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ProductVariantRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.ProductVariantMapper;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
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
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepository;
    private final ProductVariantMapper productVariantMapper;

    public ProductVariantResponse getProductVariantById(Long Id) {
        log.info("ProductVariantService::getProductVariantById - Execution started. [Id: {}]", Id);
        ProductVariant productVariant = productVariantRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant not found with ProductVariantId: " + Id));
        log.info("ProductVariantService::getProductVariantById - Execution completed. [ProductVariantId: {}]", Id);
        return productVariantMapper.toDto(productVariant);
    }

    public PageResponse<?> getAllProductVariant(int page, int size, String sortBy) {
        log.info("ProductVariantService::getAllProductVariant - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<ProductVariant> productVariantPage = productVariantRepository.findAll(pageable);
            List<ProductVariantResponse> ProductVariantList = productVariantPage.stream()
                    .map(productVariantMapper::toDto)
                    .toList();
            log.info("ProductVariantService::getAllProductVariant - Execution completed.");
            return PageResponse.builder()
                    .contents(ProductVariantList)
                    .size(size)
                    .page(p)
                    .totalPages(productVariantPage.getTotalPages())
                    .totalElements(productVariantPage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("ProductVariantService::getAllProductVariant - Execution failed.", e);
            throw new BusinessException("ProductVariantService::getAllProductVariant - Execution failed.");
        }
    }
    @Transactional
    public ProductVariantResponse createProductVariant(ProductVariantRequest productVariant) {
        log.info("ProductVariantService::createProductVariant - Execution started.");
        if (productVariantRepository.existsBySku(productVariant.getSku())) {
            throw new BusinessException("ProductVariant already exists with name: " + productVariant.getSku());
        }
        try {
            ProductVariant productVariantEntity = productVariantMapper.toEntity(productVariant);
            ProductVariant savedProductVariant = productVariantRepository.save(productVariantEntity);
            log.info("ProductVariantService::createProductVariant - Execution completed.");
            return productVariantMapper.toDto(savedProductVariant);
        } catch (Exception e) {
            log.error("ProductVariantService::createProductVariant - Execution failed.", e);
            throw new BusinessException("ProductVariantService::createProductVariant - Execution failed.");
        }
    }
    @Transactional
    public ProductVariantResponse updateProductVariantById(ProductVariantRequest productVariantRequest, Long id) {
        log.info("ProductVariantService::updateProductVariantById - Execution started.");
        try {
            ProductVariant productVariant = productVariantRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("ProductVariant not found with ProductVariantId: " + id));
            productVariant.setSku(productVariantRequest.getSku());
            productVariant.setProduct(productVariantRequest.getProduct());
            productVariant.setColor(productVariantRequest.getColor());
            productVariant.setSize(productVariantRequest.getSize());
            productVariant.setPrice(productVariantRequest.getPrice());
            productVariant.setCostPrice(productVariantRequest.getCostPrice());
            productVariant.setSalePrice(productVariantRequest.getSalePrice());
            productVariant.setQuantity(productVariantRequest.getQuantity());
            productVariant.setStatus(productVariantRequest.getStatus());
            ProductVariant savedProductVariant = productVariantRepository.save(productVariant);
            log.info("ProductVariantService::updateProfile - Execution completed. [ProductVariantId: {}]", id);
            return productVariantMapper.toDto(savedProductVariant);
        } catch (RuntimeException e) {
            log.error("ProductVariantService::updateProfile - Execution failed.", e);
            throw new BusinessException("ProductVariantService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteProductVariant(Long id) {
        log.info("ProductVariantService::deleteProductVariant - Execution started.");
        try {
            if (!productVariantRepository.existsById(id)) {
                throw new EntityNotFoundException("ProductVariant not found with ProductVariantId: " + id);
            }
            productVariantRepository.deleteById(id);
            log.info("ProductVariantService::deleteProductVariant - Execution completed.");
        } catch (Exception e) {
            log.error("ProductVariantService::deleteProductVariant - Execution failed.", e);
            throw new BusinessException("ProductVariantService::deleteProductVariant - Execution failed.");
        }
    }
}

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ProductVariantRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.ProductVariantMapper;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
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
    private final ProductRepository productRepository;
    private final ColorRepository colorRepository;
    private final SizeRepository sizeRepository;
    private final ProductVariantMapper productVariantMapper;

    // GET BY ID
    public ProductVariantResponse getProductVariantById(Long id) {
        log.info("ProductVariantService::getProductVariantById - id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant not found with id: " + id));
        return productVariantMapper.toDto(variant);
    }

    // GET ALL
    public PageResponse<?> getAllProductVariant(int page, int size, String sortBy) {
        log.info("ProductVariantService::getAllProductVariant");
        String[] sortParts = sortBy.split(",");
        Sort sort = Sort.by(Sort.Direction.fromString(sortParts[1]), sortParts[0]);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ProductVariant> variantPage = productVariantRepository.findAll(pageable);
        List<ProductVariantResponse> content = variantPage.stream()
                .map(productVariantMapper::toDto)
                .toList();
        return PageResponse.builder()
                .contents(content)
                .page(variantPage.getNumber())
                .size(variantPage.getSize())
                .totalElements(variantPage.getTotalElements())
                .totalPages(variantPage.getTotalPages())
                .build();
    }

    // CREATE
    @Transactional
    public ProductVariantResponse createProductVariant(ProductVariantRequest req) {
        log.info("ProductVariantService::createProductVariant - sku: {}", req.getSku());
        if (productVariantRepository.existsBySku(req.getSku())) {
            throw new BusinessException("ProductVariant already exists with SKU: " + req.getSku());
        }

        ProductVariant variant = new ProductVariant();
        variant.setSku(req.getSku());
        variant.setPrice(req.getPrice());
        variant.setCostPrice(req.getCostPrice());
        variant.setSalePrice(req.getSalePrice());
        variant.setQuantity(req.getQuantity());
        variant.setStatus(req.getStatus());

        if (req.getProduct() != null) {
            variant.setProduct(productRepository.findById(req.getProduct())
                    .orElseThrow(() -> new BusinessException("Product not found")));
        }
        if (req.getColor() != null) {
            variant.setColor(colorRepository.findById(req.getColor())
                    .orElseThrow(() -> new BusinessException("Color not found")));
        }
        if (req.getSize() != null) {
            variant.setSize(sizeRepository.findById(req.getSize())
                    .orElseThrow(() -> new BusinessException("Size not found")));
        }

        ProductVariant saved = productVariantRepository.save(variant);
        return productVariantMapper.toDto(saved);
    }

    // UPDATE
    @Transactional
    public ProductVariantResponse updateProductVariantById(ProductVariantRequest req, Long id) {
        log.info("ProductVariantService::updateProductVariantById - id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant not found with id: " + id));

        variant.setSku(req.getSku());
        variant.setPrice(req.getPrice());
        variant.setCostPrice(req.getCostPrice());
        variant.setSalePrice(req.getSalePrice());
        variant.setQuantity(req.getQuantity());
        variant.setStatus(req.getStatus());

        if (req.getProduct() != null) {
            variant.setProduct(productRepository.findById(req.getProduct())
                    .orElseThrow(() -> new EntityNotFoundException("Product not found")));
        }
        if (req.getColor() != null) {
            variant.setColor(colorRepository.findById(req.getColor())
                    .orElseThrow(() -> new EntityNotFoundException("Color not found")));
        }
        if (req.getSize() != null) {
            variant.setSize(sizeRepository.findById(req.getSize())
                    .orElseThrow(() -> new EntityNotFoundException("Size not found")));
        }

        ProductVariant saved = productVariantRepository.save(variant);
        return productVariantMapper.toDto(saved);
    }

    // DELETE
    @Transactional
    public void deleteProductVariant(Long id) {
        log.info("ProductVariantService::deleteProductVariant - id: {}", id);
        if (!productVariantRepository.existsById(id)) {
            throw new EntityNotFoundException("ProductVariant not found with id: " + id);
        }
        productVariantRepository.deleteById(id);
    }

    // UPDATE STATUS
    @Transactional
    public ProductVariantResponse updateStatus(Long id, String status) {
        log.info("ProductVariantService::updateStatus - id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ProductVariant not found with id: " + id));
        variant.setStatus(variant.getStatus().valueOf(status.toUpperCase()));
        ProductVariant saved = productVariantRepository.save(variant);
        return productVariantMapper.toDto(saved);
    }

    // BULK UPDATE STATUS
    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        log.info("ProductVariantService::bulkUpdateStatus");
        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
        variants.forEach(v -> v.setStatus(v.getStatus().valueOf(status.toUpperCase())));
        productVariantRepository.saveAll(variants);
    }

    // BULK DELETE
    @Transactional
    public void bulkDelete(List<Long> ids) {
        log.info("ProductVariantService::bulkDelete");
        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
        if (variants.size() != ids.size()) {
            throw new EntityNotFoundException("Một hoặc nhiều ProductVariant không tồn tại.");
        }
        productVariantRepository.deleteAllInBatch(variants);
    }
}
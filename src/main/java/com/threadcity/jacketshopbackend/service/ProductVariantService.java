package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.ProductVariantFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ProductVariantRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ProductVariantMapper;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
import com.threadcity.jacketshopbackend.specification.ProductVariantSpecification;
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));
        return productVariantMapper.toDto(variant);
    }

    // GET ALL
    public PageResponse<?> getAllProductVariant(ProductVariantFilterRequest request) {
        log.info("ProductVariantService::getAllProductVariant");
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<ProductVariant> spec = ProductVariantSpecification.buildSpec(request);
        Page<ProductVariant> productVariantPage = productVariantRepository.findAll(spec, pageable);

        List<ProductVariantResponse> productVariantResponse = productVariantPage.getContent().stream()
                .map(productVariantMapper::toDto)
                .toList();
        return PageResponse.builder()
                .contents(productVariantResponse)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(productVariantPage.getTotalElements())
                .totalPages(productVariantPage.getTotalPages())
                .build();
    }

    // CREATE
    @Transactional
    public ProductVariantResponse createProductVariant(ProductVariantRequest req) {
        log.info("ProductVariantService::createProductVariant - sku: {}", req.getSku());
        if (productVariantRepository.existsBySku(req.getSku())) {
            throw new ResourceConflictException(ErrorCodes.PRODUCT_VARIANT_SKU_DUPLICATE,
                    "ProductVariant already exists with SKU: " + req.getSku());
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
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found")));
        }
        if (req.getColor() != null) {
            variant.setColor(colorRepository.findById(req.getColor())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Color not found")));
        }
        if (req.getSize() != null) {
            variant.setSize(sizeRepository.findById(req.getSize())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Size not found")));
        }

        ProductVariant saved = productVariantRepository.save(variant);
        return productVariantMapper.toDto(saved);
    }

    // UPDATE
    @Transactional
    public ProductVariantResponse updateProductVariantById(ProductVariantRequest req, Long id) {
        log.info("ProductVariantService::updateProductVariantById - id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));

        variant.setSku(req.getSku());
        variant.setPrice(req.getPrice());
        variant.setCostPrice(req.getCostPrice());
        variant.setSalePrice(req.getSalePrice());
        variant.setQuantity(req.getQuantity());
        variant.setStatus(req.getStatus());

        if (req.getProduct() != null) {
            variant.setProduct(productRepository.findById(req.getProduct())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found")));
        }
        if (req.getColor() != null) {
            variant.setColor(colorRepository.findById(req.getColor())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Color not found")));
        }
        if (req.getSize() != null) {
            variant.setSize(sizeRepository.findById(req.getSize())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Size not found")));
        }

        ProductVariant saved = productVariantRepository.save(variant);
        return productVariantMapper.toDto(saved);
    }

    // DELETE
    @Transactional
    public void deleteProductVariant(Long id) {
        log.info("ProductVariantService::deleteProductVariant - id: {}", id);
        if (!productVariantRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                    "ProductVariant not found with id: " + id);
        }
        productVariantRepository.deleteById(id);
    }

    // UPDATE STATUS
    @Transactional
    public ProductVariantResponse updateStatus(Long id, String status) {
        log.info("ProductVariantService::updateStatus - id: {}", id);
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));
        variant.setStatus(Enums.Status.valueOf(status.toUpperCase()));
        ProductVariant saved = productVariantRepository.save(variant);
        return productVariantMapper.toDto(saved);
    }

    // BULK UPDATE STATUS
    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        log.info("ProductVariantService::bulkUpdateStatus");
        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
        if (variants.size() != ids.size()) {
            // Ideally we should find which ones are missing, but for bulk ops, reporting
            // mismatch is often enough or partial success.
            // Here we just warn or fail. The original code didn't check size equality in
            // `bulkUpdateStatus` (unlike bulkDelete).
            // But let's keep original logic for updateStatus (which had NONE).
            // Actually, `findAllById` returns what it found. It won't throw exception.
        }
        variants.forEach(v -> v.setStatus(Enums.Status.valueOf(status.toUpperCase())));
        productVariantRepository.saveAll(variants);
    }

    // BULK DELETE
    @Transactional
    public void bulkDelete(List<Long> ids) {
        log.info("ProductVariantService::bulkDelete");
        List<ProductVariant> variants = productVariantRepository.findAllById(ids);
        if (variants.size() != ids.size()) {
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                    "One or more product variants do not exist.");
        }
        productVariantRepository.deleteAllInBatch(variants);
    }
}
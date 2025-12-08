package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.filter.ProductVariantFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.ProductVariantRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ProductVariantMapper;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
import com.threadcity.jacketshopbackend.repository.MaterialRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
import com.threadcity.jacketshopbackend.specification.ProductVariantSpecification;
import jakarta.transaction.Transactional;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
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
    private final MaterialRepository materialRepository;

    public ProductVariantResponse getProductVariantById(Long id) {
        log.info("ProductVariantService::getProductVariantById - Execution started.");
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));
        log.info("ProductVariantService::getProductVariantById - Execution completed.");

        return productVariantMapper.toDto(variant);
    }

    public PageResponse<?> getAllProductVariants(ProductVariantFilterRequest request) {
        log.info("ProductVariantService::getAllProductVariants - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<ProductVariant> spec = ProductVariantSpecification.buildSpec(request);
        Page<ProductVariant> productVariantPage = productVariantRepository.findAll(spec, pageable);

        List<ProductVariantResponse> productVariantResponse = productVariantPage.getContent().stream()
                .map(productVariantMapper::toDto)
                .toList();

        log.info("ProductVariantService::getAllProductVariants - Execution completed.");

        return PageResponse.builder()
                .contents(productVariantResponse)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(productVariantPage.getTotalElements())
                .totalPages(productVariantPage.getTotalPages())
                .build();
    }

    @Transactional
    public ProductVariantResponse createProductVariant(ProductVariantRequest req) {
        log.info("ProductVariantService::createProductVariant - Execution started.");
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

        if (req.getProductId() != null) {
            variant.setProduct(productRepository.findById(req.getProductId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found")));
        }
        if (req.getColorId() != null) {
            variant.setColor(colorRepository.findById(req.getColorId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Color not found")));
        }
        if (req.getSizeId() != null) {
            variant.setSize(sizeRepository.findById(req.getSizeId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Size not found")));
        }
        if (req.getMaterialId() != null) {
            variant.setMaterial(materialRepository.findById(req.getMaterialId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND, "Material not found")));
        }

        ProductVariant saved = productVariantRepository.save(variant);
        log.info("ProductVariantService::createProductVariant - Execution completed.");
        return productVariantMapper.toDto(saved);
    }

    @Transactional
    public ProductVariantResponse updateProductVariantById(ProductVariantRequest req, Long id) {
        log.info("ProductVariantService::updateProductVariantById - Execution started.");
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));

        variant.setSku(req.getSku());
        variant.setPrice(req.getPrice());
        variant.setCostPrice(req.getCostPrice());
        variant.setSalePrice(req.getSalePrice());
        variant.setQuantity(req.getQuantity());
        variant.setStatus(req.getStatus());

        if (req.getProductId() != null) {
            variant.setProduct(productRepository.findById(req.getProductId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found")));
        }
        if (req.getColorId() != null) {
            variant.setColor(colorRepository.findById(req.getColorId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Color not found")));
        }
        if (req.getSizeId() != null) {
            variant.setSize(sizeRepository.findById(req.getSizeId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Size not found")));
        }
        if (req.getMaterialId() != null) {
            variant.setMaterial(materialRepository.findById(req.getMaterialId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND, "Material not found")));
        }

        ProductVariant saved = productVariantRepository.save(variant);
        log.info("ProductVariantService::updateProductVariantById - Execution completed.");
        return productVariantMapper.toDto(saved);
    }

    @Transactional
    public void deleteProductVariant(Long id) {
        log.info("ProductVariantService::deleteProductVariant - Execution started.");
        if (!productVariantRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                    "ProductVariant not found with id: " + id);
        }
        productVariantRepository.deleteById(id);
        log.info("ProductVariantService::deleteProductVariant - Execution completed.");
    }

    @Transactional
    public ProductVariantResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("ProductVariantService::updateStatus - Execution started.");
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));
        variant.setStatus(request.getStatus());
        ProductVariant saved = productVariantRepository.save(variant);
        log.info("ProductVariantService::updateStatus - Execution completed.");
        return productVariantMapper.toDto(saved);
    }

    @Transactional
    public List<ProductVariantResponse> bulkUpdateProductVariantsStatus(BulkStatusRequest request) {
        log.info("ProductVariantService::bulkUpdateProductVariantsStatus - Execution started.");

        List<ProductVariant> variants = productVariantRepository.findAllById(request.getIds());
        if (variants.size() != request.getIds().size()) {
            Set<Long> foundIds = variants.stream().map(ProductVariant::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                    "ProductVariants not found: " + missingIds);
        }

        variants.forEach(v -> v.setStatus(request.getStatus()));
        List<ProductVariant> savedVariants = productVariantRepository.saveAll(variants);

        log.info("ProductVariantService::bulkUpdateProductVariantsStatus - Execution completed.");
        return savedVariants.stream().map(productVariantMapper::toDto).toList();
    }

    @Transactional
    public void bulkDeleteProductVariants(BulkDeleteRequest request) {
        log.info("ProductVariantService::bulkDeleteProductVariants - Execution started.");

        List<ProductVariant> variants = productVariantRepository.findAllById(request.getIds());

        if (variants.size() != request.getIds().size()) {
            Set<Long> foundIds = variants.stream().map(ProductVariant::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                    "ProductVariants not found: " + missingIds);
        }

        productVariantRepository.deleteAllInBatch(variants);

        log.info("ProductVariantService::bulkDeleteProductVariants - Execution completed.");
    }
}
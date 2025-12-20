package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.filter.ProductVariantFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.ProductVariantCreateRequest;
import com.threadcity.jacketshopbackend.dto.request.ProductVariantUpdateRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductVariantResponse;
import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.entity.Material;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Size;

import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ProductVariantMapper;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
import com.threadcity.jacketshopbackend.repository.MaterialRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
import com.threadcity.jacketshopbackend.specification.ProductVariantSpecification;
import com.threadcity.jacketshopbackend.utils.SkuUtils;

import jakarta.transaction.Transactional;
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
    private final ProductService productService;
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

    public List<ProductVariantResponse> getAllProductVariantsByProductId(Long productId) {
        log.info("ProductVariantService::getAllProductVariantsByProductId - Execution started.");

        List<ProductVariantResponse> productVariantResponse = productVariantRepository.findAllByProductId(productId)
                .stream().map(productVariantMapper::toDto).toList();

        log.info("ProductVariantService::getAllProductVariantsByProductId - Execution completed.");

        return productVariantResponse;
    }

    public ProductVariantResponse getProductVariantBySku(String sku) {
        log.info("ProductVariantService::getProductVariantBySku - Execution started. [sku: {}]", sku);
        ProductVariantResponse response = productVariantRepository.findBySku(sku)
                .map(productVariantMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with sku: " + sku));
        log.info("ProductVariantService::getProductVariantBySku - Execution completed. [sku: {}]", sku);
        return response;
    }

    @Transactional
    public ProductVariantResponse createProductVariant(ProductVariantCreateRequest req) {
        log.info("ProductVariantService::createProductVariant - Execution started.");

        if (productVariantRepository.existsByProductIdAndColorIdAndSizeIdAndMaterialId(
                req.getProductId(), req.getColorId(), req.getSizeId(), req.getMaterialId())) {
            throw new ResourceConflictException(ErrorCodes.PRODUCT_VARIANT_DUPLICATE,
                    "ProductVariant already exists with this configuration");
        }

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found"));

        Color color = colorRepository.findById(req.getColorId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Color not found"));

        Size size = sizeRepository.findById(req.getSizeId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Size not found"));

        Material material = materialRepository.findById(req.getMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND, "Material not found"));

        ProductVariant variant = new ProductVariant();

        String baseSku = SkuUtils.generateBaseSku(
                product.getName(), size.getName(), color.getName(), material.getName());
        String uniqueSku = SkuUtils.generateUniqueSku(
                baseSku, productVariantRepository::existsBySku);
        variant.setSku(uniqueSku);

        variant.setPrice(req.getPrice());
        variant.setCostPrice(req.getCostPrice());
        variant.setQuantity(req.getQuantity());
        variant.setStatus(req.getStatus());
        variant.setImage(req.getImage());
        variant.setAvailableQuantity(req.getQuantity());

        if (req.getWeight() != null)
            variant.setWeight(req.getWeight());
        if (req.getLength() != null)
            variant.setLength(req.getLength());
        if (req.getWidth() != null)
            variant.setWidth(req.getWidth());
        if (req.getHeight() != null)
            variant.setHeight(req.getHeight());

        variant.setProduct(product);
        variant.setColor(color);
        variant.setSize(size);
        variant.setMaterial(material);

        ProductVariant saved = productVariantRepository.save(variant);
        productService.syncProductData(saved.getProduct().getId());
        log.info("ProductVariantService::createProductVariant - Execution completed.");
        return productVariantMapper.toDto(saved);
    }

    @Transactional
    public ProductVariantResponse updateProductVariantById(ProductVariantUpdateRequest req, Long id) {
        log.info("ProductVariantService::updateProductVariantById - Execution started.");
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));

        // SKU and Identity Attributes are immutable.
        variant.setPrice(req.getPrice());
        variant.setCostPrice(req.getCostPrice());
        
        // Recalculate available quantity when quantity is updated
        variant.setQuantity(req.getQuantity());
        int reserved = variant.getReservedQuantity() != null ? variant.getReservedQuantity() : 0;
        variant.setAvailableQuantity(req.getQuantity() - reserved);

        variant.setStatus(req.getStatus());
        variant.setImage(req.getImage());

        if (req.getWeight() != null)
            variant.setWeight(req.getWeight());
        if (req.getLength() != null)
            variant.setLength(req.getLength());
        if (req.getWidth() != null)
            variant.setWidth(req.getWidth());
        if (req.getHeight() != null)
            variant.setHeight(req.getHeight());

        ProductVariant saved = productVariantRepository.save(variant);
        productService.syncProductData(saved.getProduct().getId());
        log.info("ProductVariantService::updateProductVariantById - Execution completed.");
        return productVariantMapper.toDto(saved);
    }

    @Transactional
    public void adjustStock(Long variantId, int quantityChange) {
        log.info("ProductVariantService::adjustStock - Execution started. [id: {}, change: {}]", variantId, quantityChange);
        if (!productVariantRepository.existsById(variantId)) {
             throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND, "ProductVariant not found");
        }
        productVariantRepository.adjustStock(variantId, quantityChange);
        log.info("ProductVariantService::adjustStock - Execution completed.");
    }

    public boolean isVariantAvailable(Long id, int requestedQuantity) {
        return productVariantRepository.findById(id)
                .map(variant -> variant.getStatus() == Status.ACTIVE 
                        && variant.getAvailableQuantity() >= requestedQuantity)
                .orElse(false);
    }

    @Transactional
    public void reserveStock(List<CartItemRequest> items) {
        log.info("ProductVariantService::reserveStock - Execution started. [items: {}]", items.size());
        for (CartItemRequest item : items) {
            int updatedRows = productVariantRepository.reserveStock(item.getProductVariantId(), item.getQuantity());
            if (updatedRows == 0) {
                 throw new InvalidRequestException(ErrorCodes.PRODUCT_OUT_OF_STOCK, 
                         "Not enough stock for variant ID: " + item.getProductVariantId());
            }
        }
        log.info("ProductVariantService::reserveStock - Execution completed.");
    }

    @Transactional
    public void releaseReservedStock(List<OrderDetail> details) {
        log.info("ProductVariantService::releaseReservedStock - Execution started. [details: {}]", details.size());
        for (OrderDetail detail : details) {
            productVariantRepository.releaseReservedStock(detail.getProductVariant().getId(), detail.getQuantity());
        }
        log.info("ProductVariantService::releaseReservedStock - Execution completed.");
    }

    @Transactional
    public void commitReservedStock(List<OrderDetail> details) {
        log.info("ProductVariantService::commitReservedStock - Execution started. [details: {}]", details.size());
         for (OrderDetail detail : details) {
            productVariantRepository.commitReservedStock(detail.getProductVariant().getId(), detail.getQuantity());
        }
        log.info("ProductVariantService::commitReservedStock - Execution completed.");
    }

    @Transactional
    public void directDeductStock(List<CartItemRequest> items) {
        log.info("ProductVariantService::directDeductStock - Execution started. [items: {}]", items.size());
        for (CartItemRequest item : items) {
            int updatedRows = productVariantRepository.directDeductStock(item.getProductVariantId(), item.getQuantity());
             if (updatedRows == 0) {
                 throw new InvalidRequestException(ErrorCodes.PRODUCT_OUT_OF_STOCK, 
                         "Not enough stock for variant ID: " + item.getProductVariantId());
            }
        }
        log.info("ProductVariantService::directDeductStock - Execution completed.");
    }

    @Transactional
    public void deleteProductVariant(Long id) {
        log.info("ProductVariantService::deleteProductVariant - Execution started.");
        ProductVariant variant = productVariantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + id));

        Long productId = variant.getProduct().getId();
        productVariantRepository.delete(variant);

        productService.syncProductData(productId);
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
        productService.syncProductData(saved.getProduct().getId());
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

        Set<Long> productIds = savedVariants.stream()
                .map(v -> v.getProduct().getId())
                .collect(Collectors.toSet());
        productIds.forEach(productService::syncProductData);

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

        Set<Long> productIds = variants.stream()
                .map(v -> v.getProduct().getId())
                .collect(Collectors.toSet());

        productVariantRepository.deleteAllInBatch(variants);

        productIds.forEach(productService::syncProductData);

        log.info("ProductVariantService::bulkDeleteProductVariants - Execution completed.");
    }
}

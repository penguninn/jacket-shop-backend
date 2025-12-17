package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.SaleRequest;
import com.threadcity.jacketshopbackend.dto.response.SaleResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public SaleResponse applySale(SaleRequest request) {
        log.info("SaleService::applySale - Execution started. [variantId: {}]", request.getVariantId());

        ProductVariant variant = productVariantRepository.findById(request.getVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + request.getVariantId()));

        variant.setSaleStartDate(request.getSaleStartDate());
        variant.setSaleEndDate(request.getSaleEndDate());
        variant.setDiscountPercentage(request.getDiscountPercentage());

        if (request.getDiscountPercentage() != null && variant.getPrice() != null) {
            BigDecimal discountFactor = request.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2,
                    RoundingMode.HALF_UP);
            BigDecimal discountAmount = variant.getPrice().multiply(discountFactor);
            variant.setSalePrice(variant.getPrice().subtract(discountAmount));
        } else {
            variant.setSalePrice(null);
        }

        ProductVariant saved = productVariantRepository.save(variant);
        log.info("SaleService::applySale - Execution completed.");
        return mapToDto(saved);
    }

    public SaleResponse getSale(Long variantId) {
        log.info("SaleService::getSale - Execution started. [variantId: {}]", variantId);
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + variantId));
        log.info("SaleService::getSale - Execution completed.");
        return mapToDto(variant);
    }

    public List<SaleResponse> getAllSales() {
        log.info("SaleService::getAllSales - Execution started.");
        List<ProductVariant> variants = productVariantRepository
                .findByDiscountPercentageGreaterThan(BigDecimal.ZERO);
        log.info("SaleService::getAllSales - Execution completed. Found {} sales.", variants.size());
        return variants.stream().map(this::mapToDto).toList();
    }

    @Transactional
    public void removeSale(Long variantId) {
        log.info("SaleService::removeSale - Execution started. [variantId: {}]", variantId);
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "ProductVariant not found with id: " + variantId));

        variant.setSaleStartDate(null);
        variant.setSaleEndDate(null);
        variant.setDiscountPercentage(null);
        variant.setSalePrice(null);

        productVariantRepository.save(variant);
        log.info("SaleService::removeSale - Execution completed.");
    }

    private SaleResponse mapToDto(ProductVariant variant) {
        return SaleResponse.builder()
                .variantId(variant.getId())
                .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                .sku(variant.getSku())
                .image(variant.getImage())
                .originalPrice(variant.getPrice())
                .saleStartDate(variant.getSaleStartDate())
                .saleEndDate(variant.getSaleEndDate())
                .salePrice(variant.getSalePrice())
                .discountPercentage(variant.getDiscountPercentage())
                .build();
    }
}

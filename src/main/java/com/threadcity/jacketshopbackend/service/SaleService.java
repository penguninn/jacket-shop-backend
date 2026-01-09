package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.common.request.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.promotion.request.SaleCreateRequest;
import com.threadcity.jacketshopbackend.dto.promotion.request.SaleUpdateRequest;
import com.threadcity.jacketshopbackend.dto.promotion.response.SaleResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Sale;
import com.threadcity.jacketshopbackend.entity.SaleVariant;
import com.threadcity.jacketshopbackend.entity.SaleVariantId;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.SaleFilterRequest;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.SaleRepository;
import com.threadcity.jacketshopbackend.specification.SaleSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final ProductVariantRepository productVariantRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public SaleResponse createSale(SaleCreateRequest request) {
        log.info("SaleService::createSale - Execution started. [variantIds: {}]", request.getProductVariantIds());

        List<ProductVariant> variants = productVariantRepository.findAllById(request.getProductVariantIds());
        if (variants.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND, "No variants found with provided IDs");
        }

        Sale sale = Sale.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .discountPercentage(request.getDiscountPercentage())
                .saleVariants(new java.util.LinkedHashSet<>())
                .build();

        Sale savedSale = saleRepository.save(sale);

        // Create SaleVariant join entities
        for (ProductVariant variant : variants) {
            SaleVariantId saleVariantId = SaleVariantId.builder()
                    .saleId(savedSale.getId())
                    .productVariantId(variant.getId())
                    .build();

            SaleVariant saleVariant = SaleVariant.builder()
                    .id(saleVariantId)
                    .sale(savedSale)
                    .productVariant(variant)
                    .build();

            savedSale.getSaleVariants().add(saleVariant);
        }

        Sale result = saleRepository.save(savedSale);
        log.info("SaleService::createSale - Execution completed.");
        return mapToDto(result);
    }

    @Transactional
    public SaleResponse updateSale(Long id, SaleUpdateRequest request) {
        log.info("SaleService::updateSale - Execution started. [id: {}]", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND,
                        "Sale not found with id: " + id));

        sale.setName(request.getName());
        sale.setDescription(request.getDescription());
        sale.setStartDate(request.getStartDate());
        sale.setEndDate(request.getEndDate());
        sale.setDiscountPercentage(request.getDiscountPercentage());

        Sale savedSale = saleRepository.save(sale);
        log.info("SaleService::updateSale - Execution completed.");
        return mapToDto(savedSale);
    }

    public SaleResponse getSaleById(Long saleId) {
        log.info("SaleService::getSaleById - Execution started. [saleId: {}]", saleId);
        
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, 
                        "Sale not found with id: " + saleId));
        
        log.info("SaleService::getSaleById - Execution completed.");
        return mapToDto(sale);
    }

    public PageResponse<List<SaleResponse>> getAllSales(SaleFilterRequest request) {
        log.info("SaleService::getAllSales - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Sale> spec = SaleSpecification.buildSpec(request);
        Page<Sale> salePage = saleRepository.findAll(spec, pageable);

        List<SaleResponse> saleResponses = salePage.getContent().stream()
                .map(this::mapToDto)
                .toList();

        log.info("SaleService::getAllSales - Execution completed. Found {} sales.", saleResponses.size());
        return PageResponse.<List<SaleResponse>>builder()
                .contents(saleResponses)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(salePage.getTotalPages())
                .totalElements(salePage.getTotalElements())
                .build();
    }

    @Transactional
    public void deleteSale(Long saleId) {
        log.info("SaleService::deleteSale - Execution started. [saleId: {}]", saleId);

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND,
                        "Sale not found with id: " + saleId));

        // Cascade delete will handle SaleVariants
        saleRepository.delete(sale);
        log.info("SaleService::deleteSale - Execution completed.");
    }

    @Transactional
    public void bulkDeleteSales(BulkDeleteRequest request) {
        log.info("SaleService::bulkDeleteSales - Execution started.");

        List<Sale> sales = saleRepository.findAllById(request.getIds());

        if (sales.size() != request.getIds().size()) {
            Set<Long> foundIds = sales.stream().map(Sale::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "Sales not found: " + missingIds);
        }

        // Cascade delete will handle SaleVariants
        saleRepository.deleteAllInBatch(sales);

        log.info("SaleService::bulkDeleteSales - Execution completed.");
    }

    private SaleResponse mapToDto(Sale sale) {
        List<SaleResponse.SaleVariantDetail> variantDetails = new ArrayList<>();
        if (sale.getSaleVariants() != null) {
            for (SaleVariant saleVariant : sale.getSaleVariants()) {
                ProductVariant variant = saleVariant.getProductVariant();
                BigDecimal salePrice = null;
                if (sale.getDiscountPercentage() != null && variant.getPrice() != null) {
                    BigDecimal discountFactor = sale.getDiscountPercentage().divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal discountAmount = variant.getPrice().multiply(discountFactor);
                    salePrice = variant.getPrice().subtract(discountAmount);
                }

                variantDetails.add(SaleResponse.SaleVariantDetail.builder()
                        .variantId(variant.getId())
                        .productName(variant.getProduct() != null ? variant.getProduct().getName() : null)
                        .sku(variant.getSku())
                        .image(variant.getImage())
                        .originalPrice(variant.getPrice())
                        .salePrice(salePrice)
                        .build());
            }
        }

        return SaleResponse.builder()
                .id(sale.getId())
                .name(sale.getName())
                .description(sale.getDescription())
                .startDate(sale.getStartDate())
                .endDate(sale.getEndDate())
                .discountPercentage(sale.getDiscountPercentage())
                .variants(variantDetails)
                .build();
    }
}

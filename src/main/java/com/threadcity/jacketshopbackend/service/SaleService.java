package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.SaleRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SaleResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Sale;
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
    public SaleResponse createSale(SaleRequest request) {
        log.info("SaleService::createSale - Execution started. [variantIds: {}]", request.getProductVariantIds());

        List<ProductVariant> variants = productVariantRepository.findAllById(request.getProductVariantIds());
        if (variants.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND, "No variants found with provided IDs");
        }

        Sale sale = Sale.builder()
                .name(request.getName())
                .description(request.getDescription())
                .startDate(request.getSaleStartDate())
                .endDate(request.getSaleEndDate())
                .discountPercentage(request.getDiscountPercentage())
                .productVariants(new ArrayList<>())
                .build();

        Sale savedSale = saleRepository.save(sale);

        for (ProductVariant variant : variants) {
            variant.getSales().add(savedSale);
            productVariantRepository.save(variant);
            savedSale.getProductVariants().add(variant);
        }

        log.info("SaleService::createSale - Execution completed.");
        return mapToDto(savedSale);
    }

    @Transactional
    public SaleResponse updateSale(Long id, SaleRequest request) {
        log.info("SaleService::updateSale - Execution started. [id: {}]", id);

        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND,
                        "Sale not found with id: " + id));

        sale.setName(request.getName());
        sale.setDescription(request.getDescription());
        sale.setStartDate(request.getSaleStartDate());
        sale.setEndDate(request.getSaleEndDate());
        sale.setDiscountPercentage(request.getDiscountPercentage());

        // Update variants if provided
        if (request.getProductVariantIds() != null) {
            // Unlink old variants
            if (sale.getProductVariants() != null) {
                for (ProductVariant variant : sale.getProductVariants()) {
                    variant.getSales().remove(sale);
                    productVariantRepository.save(variant);
                }
                sale.getProductVariants().clear();
            }

            // Link new variants
            List<ProductVariant> variants = productVariantRepository.findAllById(request.getProductVariantIds());
            if (variants.isEmpty() && !request.getProductVariantIds().isEmpty()) {
                 throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND, "No variants found with provided IDs");
            }
            
            for (ProductVariant variant : variants) {
                variant.getSales().add(sale);
                productVariantRepository.save(variant);
                if (sale.getProductVariants() == null) {
                    sale.setProductVariants(new ArrayList<>());
                }
                sale.getProductVariants().add(variant);
            }
        } else {
             // If variants are not being updated, we still need to sync because discount/dates might have changed
        }

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

        // Unlink variants
        if (sale.getProductVariants() != null) {
            for (ProductVariant variant : sale.getProductVariants()) {
                variant.getSales().remove(sale);
                productVariantRepository.save(variant);
            }
        }

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

        // Unlink variants for all sales
        for (Sale sale : sales) {
             if (sale.getProductVariants() != null) {
                for (ProductVariant variant : sale.getProductVariants()) {
                    variant.getSales().remove(sale);
                    productVariantRepository.save(variant);
                }
            }
        }

        saleRepository.deleteAllInBatch(sales);

        log.info("SaleService::bulkDeleteSales - Execution completed.");
    }

    private SaleResponse mapToDto(Sale sale) {
        List<SaleResponse.SaleVariantDetail> variantDetails = new ArrayList<>();
        if (sale.getProductVariants() != null) {
            for (ProductVariant variant : sale.getProductVariants()) {
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

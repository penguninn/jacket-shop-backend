package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.SaleRequest;
import com.threadcity.jacketshopbackend.dto.response.SaleResponse;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Sale;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final ProductVariantRepository productVariantRepository;
    private final SaleRepository saleRepository;

    @Transactional
    public SaleResponse applySale(SaleRequest request) {
        log.info("SaleService::applySale - Execution started. [variantIds: {}]", request.getProductVariantIds());

        List<ProductVariant> variants = productVariantRepository.findAllById(request.getProductVariantIds());
        if (variants.isEmpty()) {
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND, "No variants found with provided IDs");
        }

        Sale sale = Sale.builder()
                .startDate(request.getSaleStartDate())
                .endDate(request.getSaleEndDate())
                .discountPercentage(request.getDiscountPercentage())
                .productVariants(new ArrayList<>())
                .build();

        Sale savedSale = saleRepository.save(sale);

        for (ProductVariant variant : variants) {
            variant.setSale(savedSale);
            productVariantRepository.save(variant);
            savedSale.getProductVariants().add(variant);
        }

        log.info("SaleService::applySale - Execution completed.");
        return mapToDto(savedSale);
    }

    public SaleResponse getSale(Long saleId) {
        log.info("SaleService::getSale - Execution started. [saleId: {}]", saleId);
        
        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, 
                        "Sale not found with id: " + saleId));
        
        log.info("SaleService::getSale - Execution completed.");
        return mapToDto(sale);
    }

    public List<SaleResponse> getAllSales() {
        log.info("SaleService::getAllSales - Execution started.");
        List<Sale> sales = saleRepository.findAll();
        log.info("SaleService::getAllSales - Execution completed. Found {} sales.", sales.size());
        return sales.stream().map(this::mapToDto).toList();
    }

    @Transactional
    public void removeSale(Long saleId) {
        log.info("SaleService::removeSale - Execution started. [saleId: {}]", saleId);

        Sale sale = saleRepository.findById(saleId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND,
                        "Sale not found with id: " + saleId));

        // Unlink variants
        if (sale.getProductVariants() != null) {
            for (ProductVariant variant : sale.getProductVariants()) {
                variant.setSale(null);
                productVariantRepository.save(variant);
            }
        }

        saleRepository.delete(sale);
        log.info("SaleService::removeSale - Execution completed.");
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

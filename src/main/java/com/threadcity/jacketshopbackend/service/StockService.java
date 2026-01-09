package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.entity.OrderDetail;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StockService {

    private final ProductVariantRepository productVariantRepository;

    @Transactional
    public void reserveStock(Long variantId, Integer quantity) {
        log.debug("StockService::reserveStock - variantId: {}, quantity: {}", variantId, quantity);
        if (quantity <= 0) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Quantity must be positive");
        }

        int updatedRows = productVariantRepository.reserveStock(variantId, quantity);

        if (updatedRows == 0) {
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                            "ProductVariant not found: " + variantId));

            log.warn("StockService::reserveStock - Insufficient stock. variantId: {}, requested: {}, available: {}",
                    variantId, quantity, variant.getAvailableQuantity());

            throw new InvalidRequestException(ErrorCodes.PRODUCT_OUT_OF_STOCK,
                    String.format("Insufficient stock for variant %s. Available: %d, Requested: %d",
                            variant.getSku(), variant.getAvailableQuantity(), quantity));
        }
        log.info("StockService::reserveStock - Reserved {} units for variantId: {}", quantity, variantId);
    }

    @Transactional
    public void releaseReservedStock(Long variantId, Integer quantity) {
        log.debug("StockService::releaseReservedStock - variantId: {}, quantity: {}", variantId, quantity);
        if (quantity <= 0) {
            return;
        }
        productVariantRepository.releaseReservedStock(variantId, quantity);
        log.info("StockService::releaseReservedStock - Released {} units for variantId: {}", quantity, variantId);
    }

    @Transactional
    public void releaseReservedStock(List<OrderDetail> orderDetails) {
        log.debug("StockService::releaseReservedStock - Releasing stock for {} items", orderDetails.size());
        for (OrderDetail detail : orderDetails) {
            releaseReservedStock(detail.getProductVariant().getId(), detail.getQuantity());
        }
    }

    @Transactional
    public void commitReservedStock(Long variantId, Integer quantity) {
        log.debug("StockService::commitReservedStock - variantId: {}, quantity: {}", variantId, quantity);
        if (quantity <= 0) {
            return;
        }
        productVariantRepository.commitReservedStock(variantId, quantity);
        log.info("StockService::commitReservedStock - Committed {} units for variantId: {}", quantity, variantId);
    }

    @Transactional
    public void commitReservedStock(List<OrderDetail> orderDetails) {
        log.debug("StockService::commitReservedStock - Committing stock for {} items", orderDetails.size());
        for (OrderDetail detail : orderDetails) {
            commitReservedStock(detail.getProductVariant().getId(), detail.getQuantity());
        }
    }

    @Transactional
    public void directDeductStock(Long variantId, Integer quantity) {
        log.debug("StockService::directDeductStock - variantId: {}, quantity: {}", variantId, quantity);

        if (quantity <= 0) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Quantity must be positive");
        }

        int updatedRows = productVariantRepository.directDeductStock(variantId, quantity);

        if (updatedRows == 0) {
            ProductVariant variant = productVariantRepository.findById(variantId)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                            "ProductVariant not found: " + variantId));

            log.warn("StockService::directDeductStock - Insufficient stock. variantId: {}, requested: {}, available: {}",
                    variantId, quantity, variant.getAvailableQuantity());

            throw new InvalidRequestException(ErrorCodes.PRODUCT_OUT_OF_STOCK,
                    String.format("Insufficient stock for variant %s. Available: %d, Requested: %d",
                            variant.getSku(), variant.getAvailableQuantity(), quantity));
        }

        log.info("StockService::directDeductStock - Deducted {} units for variantId: {}", quantity, variantId);
    }

    @Transactional
    public void directDeductStock(List<OrderDetail> orderDetails) {
        log.debug("StockService::directDeductStock - Deducting stock for {} items", orderDetails.size());

        for (OrderDetail detail : orderDetails) {
            directDeductStock(detail.getProductVariant().getId(), detail.getQuantity());
        }
    }

    @Transactional
    public void returnStock(Long variantId, Integer quantity) {
        log.debug("StockService::returnStock - variantId: {}, quantity: {}", variantId, quantity);

        if (quantity <= 0) {
            return;
        }

        productVariantRepository.returnStock(variantId, quantity);
        log.info("StockService::returnStock - Returned {} units for variantId: {}", quantity, variantId);
    }

    @Transactional
    public void returnStock(List<OrderDetail> orderDetails) {
        log.debug("StockService::returnStock - Returning stock for {} items", orderDetails.size());

        for (OrderDetail detail : orderDetails) {
            returnStock(detail.getProductVariant().getId(), detail.getQuantity());
        }
    }

    @Transactional
    public void adjustReservedStock(Long variantId, Integer oldQty, Integer newQty) {
        log.debug("StockService::adjustReservedStock - variantId: {}, oldQty: {}, newQty: {}",
                variantId, oldQty, newQty);
        int diff = newQty - oldQty;

        if (diff > 0) {
            reserveStock(variantId, diff);
        } else if (diff < 0) {
            releaseReservedStock(variantId, -diff);
        }
    }

    public boolean hasAvailableStock(Long variantId, Integer quantity) {
        ProductVariant variant = productVariantRepository.findById(variantId).orElse(null);
        if (variant == null) {
            return false;
        }
        return variant.getAvailableQuantity() >= quantity;
    }

    public Integer getAvailableQuantity(Long variantId) {
        return productVariantRepository.findById(variantId)
                .map(ProductVariant::getAvailableQuantity)
                .orElse(0);
    }
}

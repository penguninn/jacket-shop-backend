package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.filter.ProductVariantFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.List;

public class ProductVariantSpecification {

    public static Specification<ProductVariant> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank())
                return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("sku")), pattern);
        };
    }

    public static Specification<ProductVariant> hasProduct(Long productId) {
        return (root, query, cb) -> {
            if (productId == null)
                return null;
            return cb.equal(root.get("product").get("id"), productId);
        };
    }

    public static Specification<ProductVariant> hasSize(List<Long> sizeIds) {
        return (root, query, cb) -> {
            if (sizeIds == null || sizeIds.isEmpty())
                return cb.conjunction();
            return root.get("size").get("id").in(sizeIds);
        };
    }

    public static Specification<ProductVariant> hasColor(List<Long> colorIds) {
        return (root, query, cb) -> {
            if (colorIds == null || colorIds.isEmpty())
                return cb.conjunction();
            return root.get("color").get("id").in(colorIds);
        };
    }

    public static Specification<ProductVariant> hasMaterial(List<Long> materialIds) {
        return (root, query, cb) -> {
            if (materialIds == null || materialIds.isEmpty())
                return cb.conjunction();
            return root.get("material").get("id").in(materialIds);
        };
    }

    public static Specification<ProductVariant> hasPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null)
                return null;

            if (minPrice != null && maxPrice != null)
                return cb.between(root.get("price"), minPrice, maxPrice);

            if (minPrice != null)
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);

            return cb.lessThanOrEqualTo(root.get("price"), maxPrice);
        };
    }

    public static Specification<ProductVariant> hasStatuses(List<String> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty())
                return null;

            List<Status> statusEnums = statuses.stream()
                    .map(s -> Status.valueOf(s.toUpperCase()))
                    .toList();

            return root.get("status").in(statusEnums);
        };
    }

    public static Specification<ProductVariant> isInStock(Boolean inStock) {
        return (root, query, cb) -> {
            if (inStock == null)
                return null;

            if (inStock) {
                return cb.greaterThan(root.get("availableQuantity"), 0);
            } else {
                return cb.lessThanOrEqualTo(root.get("availableQuantity"), 0);
            }
        };
    }

    public static Specification<ProductVariant> isLowStock(Integer threshold) {
        return (root, query, cb) -> {
            if (threshold == null)
                return null;

            return cb.and(
                    cb.greaterThan(root.get("availableQuantity"), 0),
                    cb.lessThanOrEqualTo(root.get("availableQuantity"), threshold)
            );
        };
    }

    public static Specification<ProductVariant> hasQuantityRange(Integer minQuantity, Integer maxQuantity) {
        return (root, query, cb) -> {
            if (minQuantity == null && maxQuantity == null)
                return null;

            if (minQuantity != null && maxQuantity != null)
                return cb.between(root.get("availableQuantity"), minQuantity, maxQuantity);

            if (minQuantity != null)
                return cb.greaterThanOrEqualTo(root.get("availableQuantity"), minQuantity);

            return cb.lessThanOrEqualTo(root.get("availableQuantity"), maxQuantity);
        };
    }

    public static Specification<ProductVariant> buildSpec(ProductVariantFilterRequest request) {
        return hasSearch(request.getSearch())
                .and(hasProduct(request.getProductId()))
                .and(hasColor(request.getColorIds()))
                .and(hasSize(request.getSizeIds()))
                .and(hasMaterial(request.getMaterialIds()))
                .and(hasPriceRange(request.getMinPrice(), request.getMaxPrice()))
                .and(hasStatuses(request.getStatus()))
                .and(isInStock(request.getInStock()))
                .and(isLowStock(request.getLowStockThreshold()))
                .and(hasQuantityRange(request.getMinQuantity(), request.getMaxQuantity()));
    }
}
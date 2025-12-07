package com.threadcity.jacketshopbackend.specification;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.ProductVariantFilterRequest;
import com.threadcity.jacketshopbackend.entity.ProductVariant;

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

    public static Specification<ProductVariant> hasSize(Long sizeId) {
        return (root, query, cb) -> {
            if (sizeId == null)
                return null;
            return cb.equal(root.get("size").get("id"), sizeId);
        };
    }

    public static Specification<ProductVariant> hasColor(Long colorId) {
        return (root, query, cb) -> {
            if (colorId == null)
                return null;
            return cb.equal(root.get("color").get("id"), colorId);
        };
    }

    public static Specification<ProductVariant> hasPriceRange(BigDecimal from, BigDecimal to) {
        return (root, query, cb) -> {
            if (from == null && to == null)
                return null;

            if (from != null && to != null)
                return cb.between(root.get("price"), from, to);

            if (from != null)
                return cb.greaterThanOrEqualTo(root.get("price"), from);

            return cb.lessThanOrEqualTo(root.get("price"), to);
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

    public static Specification<ProductVariant> buildSpec(ProductVariantFilterRequest request) {
        return hasColor(request.getColorId())
                .and(hasSize(request.getSizeId()))
                .and(hasPriceRange(request.getFromPrice(), request.getToPrice()))
                .and(hasStatuses(request.getStatus()));
    }
}
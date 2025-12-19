package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.filter.ProductVariantFilterRequest;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
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
        return hasSearch(request.getSearch())
                .and(hasProduct(request.getProductId()))
                .and(hasColor(request.getColorIds()))
                .and(hasSize(request.getSizeIds()))
                .and(hasMaterial(request.getMaterialIds()))
                .and(hasPriceRange(request.getFromPrice(), request.getToPrice()))
                .and(hasStatuses(request.getStatus()));
    }
}
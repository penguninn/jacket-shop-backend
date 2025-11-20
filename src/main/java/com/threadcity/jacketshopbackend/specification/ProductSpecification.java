package com.threadcity.jacketshopbackend.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.ProductFilterRequest;
import com.threadcity.jacketshopbackend.entity.Product;

public class ProductSpecification {

    public static Specification<Product> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    public static Specification<Product> hasCategory(Long categoryId) {
        return (root, query, cb) -> {
            if (categoryId == null) {
                return null;
            }
            return cb.equal(root.get("category").get("id"), categoryId);
        };
    }

    public static Specification<Product> hasBrand(Long brandId) {
        return (root, query, cb) -> {
            if (brandId == null) {
                return null;
            }
            return cb.equal(root.get("brand").get("id"), brandId);
        };
    }

    public static Specification<Product> hasMaterial(Long materialId) {
        return (root, query, cb) -> {
            if (materialId == null) {
                return null;
            }
            return cb.equal(root.get("material").get("id"), materialId);
        };
    }

    public static Specification<Product> hasStyle(Long styleId) {
        return (root, query, cb) -> {
            if (styleId == null) {
                return null;
            }
            return cb.equal(root.get("style").get("id"), styleId);
        };
    }

    public static Specification<Product> hasStatuses(List<String> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }
            List<Status> statusEnums = statuses.stream()
                    .map(s -> Status.valueOf(s.toUpperCase()))
                    .toList();

            return root.get("status").in(statusEnums);
        };
    }

    public static Specification<Product> buildSpec(ProductFilterRequest request) {
        return Specification
                .where(hasSearch(request.getSearch()))
                .and(hasCategory(request.getCategoryId()))
                .and(hasBrand(request.getBrandId()))
                .and(hasMaterial(request.getMaterialId()))
                .and(hasStyle(request.getStyleId()))
                .and(hasStatuses(request.getStatus()));
    }
}

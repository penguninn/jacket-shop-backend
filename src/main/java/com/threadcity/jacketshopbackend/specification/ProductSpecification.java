package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.filter.ProductFilterRequest;
import com.threadcity.jacketshopbackend.entity.Product;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

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

    public static Specification<Product> hasBrands(List<Long> brandIds) {
        return (root, query, cb) -> {
            if (brandIds == null || brandIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("brand").get("id").in(brandIds);
        };
    }

    public static Specification<Product> hasStyles(List<Long> styleIds) {
        return (root, query, cb) -> {
            if (styleIds == null || styleIds.isEmpty()) {
                return cb.conjunction();
            }
            return root.get("style").get("id").in(styleIds);
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
        return hasSearch(request.getSearch())
                .and(hasBrands(request.getBrandIds()))
                .and(hasStyles(request.getStyleIds()))
                .and(hasStatuses(request.getStatus()));
    }

}

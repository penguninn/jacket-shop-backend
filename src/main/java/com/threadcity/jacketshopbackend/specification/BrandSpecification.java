package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.dto.request.BrandFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.common.Enums.Status;

public class BrandSpecification {

    public static Specification<Brand> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    public static Specification<Brand> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            return cb.equal(root.get("status"), Status.valueOf(status.toUpperCase()));
        };
    }

    public static Specification<Brand> buildSpec(BrandFilterRequest request) {
        return Specification
                .where(hasSearch(request.getSearch()))
                .and(hasStatus(request.getStatus()));
    }
}
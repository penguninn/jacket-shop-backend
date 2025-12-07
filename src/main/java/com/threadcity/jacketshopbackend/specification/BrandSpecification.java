package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.dto.request.BrandFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.common.Enums.Status;

import java.util.List;

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

    public static Specification<Brand> hasStatuses(List<String> status) {
        return (root, query, cb) -> {
            if (status == null || status.isEmpty()) {
                return null;
            }
            List<Status> statusEnums = status.stream()
                    .map(s -> Status.valueOf(s.toUpperCase()))
                    .toList();
            return root.get("status").in(statusEnums);
        };
    }

    public static Specification<Brand> buildSpec(BrandFilterRequest request) {
        return hasSearch(request.getSearch())
                .and(hasStatuses(request.getStatus()));
    }
}
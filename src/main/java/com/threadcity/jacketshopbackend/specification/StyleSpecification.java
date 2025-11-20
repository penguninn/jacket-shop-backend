package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.dto.request.StyleFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.common.Enums.Status;

public class StyleSpecification {

    public static Specification<Style> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    public static Specification<Style> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null || status.isBlank()) {
                return null;
            }
            return cb.equal(root.get("status"), Status.valueOf(status.toUpperCase()));
        };
    }

    public static Specification<Style> buildSpec(StyleFilterRequest request) {
        return Specification
                .where(hasSearch(request.getSearch()))
                .and(hasStatus(request.getStatus()));
    }
}

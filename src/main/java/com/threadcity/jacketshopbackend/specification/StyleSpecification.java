package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.dto.request.StyleFilterRequest;
import org.springframework.data.jpa.domain.Specification;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.common.Enums.Status;

import java.util.List;

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

    public static Specification<Style> hasStatuses(List<String> status) {
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

    public static Specification<Style> buildSpec(StyleFilterRequest request) {
        return Specification
                .where(hasSearch(request.getSearch()))
                .and(hasStatuses(request.getStatus()));
    }
}

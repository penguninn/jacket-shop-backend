package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.CategoryFilterRequest;
import com.threadcity.jacketshopbackend.entity.Category;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class CategorySpecification {

    public static Specification<Category> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";

            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    public static Specification<Category> hasStatuses(List<String> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }

            List<Status> statusEnums = statuses.stream()
                    .map(s -> {
                        try {
                            return Status.valueOf(s.toUpperCase());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(x -> x != null)
                    .toList();

            if (statusEnums.isEmpty())
                return null;

            return root.get("status").in(statusEnums);
        };
    }

    public static Specification<Category> buildSpec(CategoryFilterRequest request) {
        return hasSearch(request.getSearch())
                .and(hasStatuses(request.getStatus()));
    }
}

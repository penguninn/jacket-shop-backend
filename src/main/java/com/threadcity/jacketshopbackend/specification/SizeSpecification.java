package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.dto.request.SizeFilterRequest;
import com.threadcity.jacketshopbackend.entity.Size;
import com.threadcity.jacketshopbackend.common.Enums.Status;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class SizeSpecification {

    public static Specification<Size> buildSpec(SizeFilterRequest request) {
        return search(request.getSearch())
                .and(filterStatus(request.getStatus()));
    }

    private static Specification<Size> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern));
        };
    }

    private static Specification<Size> filterStatus(List<String> statuses) {
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
}

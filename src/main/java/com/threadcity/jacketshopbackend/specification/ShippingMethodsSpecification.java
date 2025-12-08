package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.filter.ShippingMethodsFilterRequest;
import com.threadcity.jacketshopbackend.entity.ShippingMethod;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class ShippingMethodsSpecification {

    public static Specification<ShippingMethod> hasSearch(String search) {
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

    public static Specification<ShippingMethod> hasStatus(List<String> statuses) {
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

    public static Specification<ShippingMethod> buildSpec(ShippingMethodsFilterRequest request) {
        return hasSearch(request.getSearch())
                .and(hasStatus(request.getStatus()));
    }
}

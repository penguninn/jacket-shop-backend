package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.PaymentMethodType;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.filter.PaymentMethodFilterRequest;
import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

public class PaymentMethodSpecification {

    public static Specification<PaymentMethod> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }

            String pattern = "%" + search.toLowerCase() + "%";

            return cb.or(
                    cb.like(cb.lower(root.get("name")), pattern),
                    cb.like(cb.lower(root.get("code")), pattern));
        };
    }

    public static Specification<PaymentMethod> hasStatuses(List<String> statuses) {
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

    public static Specification<PaymentMethod> hasTypes(List<String> types) {
        return (root, query, cb) -> {
            if (types == null || types.isEmpty()) {
                return null;
            }

            List<PaymentMethodType> typeEnums = types.stream()
                    .map(t -> {
                        try {
                            return PaymentMethodType.valueOf(t.toUpperCase());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(x -> x != null)
                    .toList();

            if (typeEnums.isEmpty())
                return null;

            return root.get("type").in(typeEnums);
        };
    }

    public static Specification<PaymentMethod> buildSpec(PaymentMethodFilterRequest request) {
        return hasSearch(request.getSearch())
                .and(hasStatuses(request.getStatus()))
                .and(hasTypes(request.getType()));
    }
}

package com.threadcity.jacketshopbackend.specification;


import com.threadcity.jacketshopbackend.dto.request.CouponFilterRequest;
import com.threadcity.jacketshopbackend.entity.Coupon;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.common.Enums.CouponType;

import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public class CouponSpecification {

    public static Specification<Coupon> buildSpec(CouponFilterRequest request) {
        return Specification.where(search(request.getSearch()))
                .and(filterStatus(request.getStatus()))
                .and(filterType(request.getType()))
                .and(filterValidFrom(request.getValidFrom()))
                .and(filterValidTo(request.getValidTo()));
    }

    private static Specification<Coupon> search(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) return null;

            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    private static Specification<Coupon> filterStatus(List<String> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) return null;

            List<Status> statusEnums = statuses.stream()
                    .map(s -> Status.valueOf(s.toUpperCase()))
                    .toList();

            return root.get("status").in(statusEnums);
        };
    }

    private static Specification<Coupon> filterType(List<String> types) {
        return (root, query, cb) -> {
            if (types == null || types.isEmpty()) return null;

            List<CouponType> typeEnums = types.stream()
                    .map(s -> CouponType.valueOf(s.toUpperCase()))
                    .toList();

            return root.get("type").in(typeEnums);
        };
    }

    private static Specification<Coupon> filterValidFrom(Instant from) {
        return (root, query, cb) -> {
            if (from == null) return null;
            return cb.greaterThanOrEqualTo(root.get("validFrom"), from);
        };
    }

    private static Specification<Coupon> filterValidTo(Instant to) {
        return (root, query, cb) -> {
            if (to == null) return null;
            return cb.lessThanOrEqualTo(root.get("validTo"), to);
        };
    }
}

package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.dto.order.request.CustomerOrderFilterRequest;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> buildSpec(OrderFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getOrderCode() != null && !request.getOrderCode().isBlank()) {
                predicates.add(cb.like(cb.upper(root.get("orderCode")), "%" + request.getOrderCode().toUpperCase() + "%"));
            }

            if (request.getOrderType() != null) {
                predicates.add(cb.equal(root.get("orderType"), request.getOrderType()));
            }

            if (request.getStatus() != null) {
                predicates.add(cb.equal(root.get("status"), request.getStatus()));
            }

            if (request.getPaymentStatus() != null) {
                predicates.add(cb.equal(root.get("paymentStatus"), request.getPaymentStatus()));
            }
            
            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
            }

            if (request.getStaffId() != null) {
                predicates.add(cb.equal(root.get("staff").get("id"), request.getStaffId()));
            }

            if (request.getStartDate() != null) {
                Instant start = request.getStartDate();
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), start));
            }

            if (request.getEndDate() != null) {
                Instant end = request.getEndDate();
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), end));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Build specification for customer's own orders
     * @param userId The authenticated user's ID
     * @param request Filter request with statuses, date range, pagination
     * @return Specification for filtering customer orders
     */
    public static Specification<Order> buildCustomerSpec(Long userId, CustomerOrderFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always filter by userId (security)
            predicates.add(cb.equal(root.get("user").get("id"), userId));

            // Filter by statuses
            if (request.getStatuses() != null && !request.getStatuses().isEmpty()) {
                predicates.add(root.get("status").in(request.getStatuses()));
            }

            // Date range filter
            if (request.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
            }

            if (request.getEndDate() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}

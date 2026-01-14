package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.dto.order.request.CustomerOrderFilterRequest;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;

public class OrderSpecification {

    public static Specification<Order> hasOrderCode(String orderCode) {
        return (root, query, cb) -> {
            if (orderCode == null || orderCode.isBlank()) {
                return null;
            }
            String pattern = "%" + orderCode.toUpperCase() + "%";
            return cb.like(cb.upper(root.get("orderCode")), pattern);
        };
    }

    public static Specification<Order> hasOrderType(OrderType orderType) {
        return (root, query, cb) -> {
            if (orderType == null) {
                return null;
            }
            return cb.equal(root.get("orderType"), orderType);
        };
    }

    public static Specification<Order> hasStatus(OrderStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return null;
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Order> hasStatuses(List<OrderStatus> statuses) {
        return (root, query, cb) -> {
            if (statuses == null || statuses.isEmpty()) {
                return null;
            }
            return root.get("status").in(statuses);
        };
    }

    public static Specification<Order> hasPaymentStatus(PaymentStatus paymentStatus) {
        return (root, query, cb) -> {
            if (paymentStatus == null) {
                return null;
            }
            return cb.equal(root.get("paymentStatus"), paymentStatus);
        };
    }

    public static Specification<Order> hasUserId(Long userId) {
        return (root, query, cb) -> {
            if (userId == null) {
                return null;
            }
            return cb.equal(root.get("user").get("id"), userId);
        };
    }

    public static Specification<Order> hasStaffId(Long staffId) {
        return (root, query, cb) -> {
            if (staffId == null) {
                return null;
            }
            return cb.equal(root.get("staff").get("id"), staffId);
        };
    }

    public static Specification<Order> hasStartDate(Instant startDate) {
        return (root, query, cb) -> {
            if (startDate == null) {
                return null;
            }
            return cb.greaterThanOrEqualTo(root.get("createdAt"), startDate);
        };
    }

    public static Specification<Order> hasEndDate(Instant endDate) {
        return (root, query, cb) -> {
            if (endDate == null) {
                return null;
            }
            return cb.lessThanOrEqualTo(root.get("createdAt"), endDate);
        };
    }

    public static Specification<Order> buildSpec(OrderFilterRequest request) {
        return hasOrderCode(request.getOrderCode())
                .and(hasOrderType(request.getOrderType()))
                .and(hasStatus(request.getStatus()))
                .and(hasPaymentStatus(request.getPaymentStatus()))
                .and(hasUserId(request.getUserId()))
                .and(hasStaffId(request.getStaffId()))
                .and(hasStartDate(request.getStartDate()))
                .and(hasEndDate(request.getEndDate()));
    }

    public static Specification<Order> buildCustomerSpec(Long userId, CustomerOrderFilterRequest request) {
        return hasUserId(userId)
                .and(hasStatuses(request.getStatuses()))
                .and(hasStartDate(request.getStartDate()))
                .and(hasEndDate(request.getEndDate()));
    }
}

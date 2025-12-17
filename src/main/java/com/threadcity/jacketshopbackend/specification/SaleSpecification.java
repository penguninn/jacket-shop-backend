package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.entity.Sale;
import com.threadcity.jacketshopbackend.filter.SaleFilterRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SaleSpecification {

    public static Specification<Sale> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank())
                return null;
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.like(cb.lower(root.get("name")), pattern);
        };
    }

    public static Specification<Sale> hasDateRange(LocalDateTime from, LocalDateTime to) {
        return (root, query, cb) -> {
            if (from == null && to == null) return null;
            
            if (from != null && to != null) {
                // Sale overlaps with [from, to]
                // (saleStart <= to) AND (saleEnd >= from)
                return cb.and(
                        cb.lessThanOrEqualTo(root.get("startDate"), to),
                        cb.greaterThanOrEqualTo(root.get("endDate"), from)
                );
            }
            
            if (from != null) {
                return cb.greaterThanOrEqualTo(root.get("endDate"), from);
            }
            
            return cb.lessThanOrEqualTo(root.get("startDate"), to);
        };
    }

    public static Specification<Sale> hasDiscountRange(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) return null;

            if (min != null && max != null)
                return cb.between(root.get("discountPercentage"), min, max);

            if (min != null)
                return cb.greaterThanOrEqualTo(root.get("discountPercentage"), min);

            return cb.lessThanOrEqualTo(root.get("discountPercentage"), max);
        };
    }

    public static Specification<Sale> buildSpec(SaleFilterRequest request) {
        return Specification.where(hasSearch(request.getSearch()))
                .and(hasDateRange(request.getFromDate(), request.getToDate()))
                .and(hasDiscountRange(request.getMinDiscount(), request.getMaxDiscount()));
    }
}

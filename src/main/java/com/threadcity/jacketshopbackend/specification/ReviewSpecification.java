
package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.entity.Review;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
@Slf4j
public class ReviewSpecification {

    public static Specification<Review> buildSpec(ReviewFilterRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.getProductId() != null) {
                predicates.add(cb.equal(root.get("product").get("id"), request.getProductId()));
            }

            if (request.getUserId() != null) {
                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
            }

            if (request.getOrderId() != null) {
                predicates.add(cb.equal(root.get("order").get("id"), request.getOrderId()));
            }

            if (request.getMinRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), request.getMinRating()));
            }

            if (request.getMaxRating() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("rating"), request.getMaxRating()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
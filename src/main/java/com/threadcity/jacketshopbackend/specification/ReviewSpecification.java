//package com.threadcity.jacketshopbackend.specification;
//
//import com.threadcity.jacketshopbackend.dto.product.request.ReviewFilterRequest;
//import com.threadcity.jacketshopbackend.entity.Review;
//import jakarta.persistence.criteria.Predicate;
//import org.springframework.data.jpa.domain.Specification;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class ReviewSpecification {
//
//    /**
//     * Build specification for admin review filtering
//     */
//    public static Specification<Review> buildSpec(ReviewFilterRequest request) {
//        return (root, query, cb) -> {
//            List<Predicate> predicates = new ArrayList<>();
//
//            // Filter by product
//            if (request.getProductId() != null) {
//                predicates.add(cb.equal(root.get("product").get("id"), request.getProductId()));
//            }
//
//            // Filter by user
//            if (request.getUserId() != null) {
//                predicates.add(cb.equal(root.get("user").get("id"), request.getUserId()));
//            }
//
//            // Filter by specific ratings (e.g., [5, 4] for 5 and 4 star reviews)
//            if (request.getRatings() != null && !request.getRatings().isEmpty()) {
//                predicates.add(root.get("rating").in(request.getRatings()));
//            }
//            // Filter by rating range
//            else {
//                if (request.getMinRating() != null) {
//                    predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), request.getMinRating()));
//                }
//
//                if (request.getMaxRating() != null) {
//                    predicates.add(cb.lessThanOrEqualTo(root.get("rating"), request.getMaxRating()));
//                }
//            }
//
//            // Date range filter
//            if (request.getStartDate() != null) {
//                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), request.getStartDate()));
//            }
//
//            if (request.getEndDate() != null) {
//                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), request.getEndDate()));
//            }
//
//            // Search in comment, userName, productName
//            if (request.getSearch() != null && !request.getSearch().isBlank()) {
//                String searchPattern = "%" + request.getSearch().toLowerCase() + "%";
//                Predicate commentMatch = cb.like(cb.lower(root.get("comment")), searchPattern);
//                Predicate userNameMatch = cb.like(cb.lower(root.get("userName")), searchPattern);
//                Predicate productNameMatch = cb.like(cb.lower(root.get("productName")), searchPattern);
//
//                predicates.add(cb.or(commentMatch, userNameMatch, productNameMatch));
//            }
//
//            return cb.and(predicates.toArray(new Predicate[0]));
//        };
//    }
//}

package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.entity.Review;
import com.threadcity.jacketshopbackend.filter.ReviewFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

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
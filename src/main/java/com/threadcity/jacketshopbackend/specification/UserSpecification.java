package com.threadcity.jacketshopbackend.specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.filter.UserFilterRequest;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class UserSpecification {

    /**
     * Search across fullName, username, email, and phone
     */
    public static Specification<User> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";

            List<Predicate> searchPredicates = new ArrayList<>();
            searchPredicates.add(cb.like(cb.lower(root.get("fullName")), pattern));
            searchPredicates.add(cb.like(cb.lower(root.get("username")), pattern));

            // Email might be null
            searchPredicates.add(cb.like(cb.lower(root.get("email")), pattern));

            // Phone search
            searchPredicates.add(cb.like(root.get("phone"), "%" + search + "%"));

            return cb.or(searchPredicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<User> hasStatuses(List<String> status) {
        return (root, query, cb) -> {
            if (status == null || status.isEmpty()) {
                return null;
            }
            List<Status> statusEnums = status.stream()
                    .map(s -> Status.valueOf(s.toUpperCase()))
                    .toList();
            return root.get("status").in(statusEnums);
        };
    }

    public static Specification<User> hasRoles(List<String> roleNames) {
        return (root, query, cb) -> {
            if (roleNames == null || roleNames.isEmpty()) {
                return null;
            }
            query.distinct(true); // Prevent duplicate rows from role join
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return roleJoin.get("name").in(roleNames);
        };
    }

    public static Specification<User> buildSpec(UserFilterRequest request) {
        return hasSearch(request.getSearch())
                .and(hasRoles(request.getRoles()))
                .and(hasStatuses(request.getStatus()));
    }
}

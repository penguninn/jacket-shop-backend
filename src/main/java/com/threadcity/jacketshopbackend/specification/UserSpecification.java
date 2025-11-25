package com.threadcity.jacketshopbackend.specification;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.UserFilterRequest;
import com.threadcity.jacketshopbackend.entity.Role;
import com.threadcity.jacketshopbackend.entity.User;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

public class UserSpecification {

    public static Specification<User> hasSearch(String search) {
        return (root, query, cb) -> {
            if (search == null || search.isBlank()) {
                return null;
            }
            String pattern = "%" + search.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("fullName")), pattern));
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
            Join<User, Role> roleJoin = root.join("roles", JoinType.INNER);
            return roleJoin.get("name").in(roleNames);
        };
    }

    public static Specification<User> buildSpec(UserFilterRequest request) {
        return Specification
                .where(hasSearch(request.getSearch()))
                .and(hasStatuses(request.getStatus()))
                .and(hasRoles(request.getRoles()));
    }
}

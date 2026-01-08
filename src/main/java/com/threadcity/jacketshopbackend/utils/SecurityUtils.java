package com.threadcity.jacketshopbackend.utils;

import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Security utility class for common authorization checks
 */
@Component
public class SecurityUtils {

    /**
     * Get current authenticated user ID
     */
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return null;
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) principal).getId();
        }

        return null;
    }

    /**
     * Check if current user is ADMIN or STAFF
     */
    public static boolean isAdminOrStaff() {
        return hasAnyRole("ADMIN", "STAFF");
    }

    /**
     * Check if current user is ADMIN
     */
    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user has specific role
     */
    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        return auth.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_" + role));
    }

    /**
     * Check if current user has any of the specified roles
     */
    public static boolean hasAnyRole(String... roles) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }

        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        for (String role : roles) {
            if (authorities.stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role))) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check if current user owns the resource or is admin/staff
     */
    public static boolean isOwnerOrAdmin(Long resourceOwnerId) {
        if (isAdminOrStaff()) {
            return true;
        }

        Long currentUserId = getCurrentUserId();
        return currentUserId != null && currentUserId.equals(resourceOwnerId);
    }

    /**
     * Verify current user owns the resource, throw exception if not
     */
    public static void requireOwnership(Long resourceOwnerId, String resourceType) {
        if (!isOwnerOrAdmin(resourceOwnerId)) {
            throw new SecurityException("You don't have permission to access this " + resourceType);
        }
    }
}

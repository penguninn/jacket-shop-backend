package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    // ============ STATISTICS QUERIES ============

    /**
     * Count total customers (users with CUSTOMER role)
     */
    @Query("""
            SELECT COUNT(DISTINCT u)
            FROM User u
            JOIN u.roles r
            WHERE r.name = 'CUSTOMER' AND u.status = 'ACTIVE'
            """)
    Long countTotalCustomers();

    /**
     * Count new customers within date range
     */
    @Query("""
            SELECT COUNT(DISTINCT u)
            FROM User u
            JOIN u.roles r
            WHERE r.name = 'CUSTOMER'
            AND u.createdAt >= :startDate
            AND u.createdAt <= :endDate
            """)
    Long countNewCustomers(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Count active customers (customers who have made at least one order)
     */
    @Query("""
            SELECT COUNT(DISTINCT o.user.id)
            FROM Order o
            WHERE o.user IS NOT NULL AND o.status = 'COMPLETED'
            """)
    Long countActiveCustomers();
}

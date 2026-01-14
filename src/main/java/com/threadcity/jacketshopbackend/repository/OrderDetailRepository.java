package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // ============ STATISTICS QUERIES ============

    /**
     * Calculate total cost from completed orders within date range
     * Cost = sum of (quantity * costPrice from ProductVariant)
     */
    @Query("""
            SELECT COALESCE(SUM(od.quantity * pv.costPrice), 0)
            FROM OrderDetail od
            JOIN od.order o
            JOIN od.productVariant pv
            WHERE o.status = 'COMPLETED'
            AND (:startDate IS NULL OR o.completedAt >= :startDate)
            AND (:endDate IS NULL OR o.completedAt <= :endDate)
            """)
    BigDecimal calculateTotalCost(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Calculate revenue per product for top selling (sum of subtotal)
     */
    @Query("""
            SELECT pv.product.id, COALESCE(SUM(od.subtotal), 0)
            FROM OrderDetail od
            JOIN od.productVariant pv
            JOIN od.order o
            WHERE o.status = 'COMPLETED'
            AND (:startDate IS NULL OR o.completedAt >= :startDate)
            AND (:endDate IS NULL OR o.completedAt <= :endDate)
            GROUP BY pv.product.id
            """)
    java.util.List<Object[]> calculateRevenueByProduct(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}

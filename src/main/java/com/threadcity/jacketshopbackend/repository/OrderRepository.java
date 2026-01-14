package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByOrderCode(String orderCode);

    List<Order> findByUserId(Long userId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.orderType IN :types AND o.staff.id = :staffId")
    long countPendingPosDrafts(@Param("status") OrderStatus status,
            @Param("types") List<OrderType> types,
            @Param("staffId") Long staffId);

    long countByOrderTypeAndStatus(OrderType orderType, OrderStatus status);

    /**
     * Find stale POS drafts for timeout processing.
     * Returns PENDING POS orders that haven't been updated since the cutoff time.
     */
    @Query("SELECT o FROM Order o WHERE o.orderType = :orderType AND o.status = :status AND o.updatedAt < :cutoff")
    List<Order> findStalePosDrafts(
            @Param("orderType") OrderType orderType,
            @Param("status") OrderStatus status,
            @Param("cutoff") Instant cutoff);

    List<Order> findByPaymentStatus(Enums.PaymentStatus paymentStatus);

    Optional<Order> findByPayosOrderCode(Long payosOrderCode);

    // ============ STATISTICS QUERIES ============

    /**
     * Calculate total revenue from completed orders within date range
     */
    @Query("""
            SELECT COALESCE(SUM(o.total), 0)
            FROM Order o
            WHERE o.status = 'COMPLETED'
            AND (:startDate IS NULL OR o.completedAt >= :startDate)
            AND (:endDate IS NULL OR o.completedAt <= :endDate)
            """)
    BigDecimal calculateTotalRevenue(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Count orders by status within date range
     */
    @Query("""
            SELECT o.status, COUNT(o)
            FROM Order o
            WHERE (:startDate IS NULL OR o.createdAt >= :startDate)
            AND (:endDate IS NULL OR o.createdAt <= :endDate)
            GROUP BY o.status
            """)
    List<Object[]> countOrdersByStatus(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Count completed orders by order type within date range
     */
    @Query("""
            SELECT o.orderType, COUNT(o)
            FROM Order o
            WHERE o.status = 'COMPLETED'
            AND (:startDate IS NULL OR o.completedAt >= :startDate)
            AND (:endDate IS NULL OR o.completedAt <= :endDate)
            GROUP BY o.orderType
            """)
    List<Object[]> countCompletedOrdersByType(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get top customers by total spent
     */
    @Query("""
            SELECT o.user.id, o.user.fullName, o.user.phone, o.user.email, o.user.avatar,
                   SUM(o.total), COUNT(o)
            FROM Order o
            WHERE o.status = 'COMPLETED'
            AND o.user IS NOT NULL
            AND (:startDate IS NULL OR o.completedAt >= :startDate)
            AND (:endDate IS NULL OR o.completedAt <= :endDate)
            GROUP BY o.user.id, o.user.fullName, o.user.phone, o.user.email, o.user.avatar
            ORDER BY SUM(o.total) DESC
            """)
    List<Object[]> findTopCustomersByRevenue(
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate,
            Pageable pageable);
}

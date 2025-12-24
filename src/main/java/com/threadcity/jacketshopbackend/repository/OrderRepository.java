package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}

package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository<Coupon,Long>, JpaSpecificationExecutor<Coupon> {
    boolean existsByCode(String code);
}

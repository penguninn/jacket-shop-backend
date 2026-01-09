package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByProductId(Long productId);

    Optional<Review> findByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);

    boolean existsByUserIdAndProductIdAndOrderId(Long userId, Long productId, Long orderId);
}
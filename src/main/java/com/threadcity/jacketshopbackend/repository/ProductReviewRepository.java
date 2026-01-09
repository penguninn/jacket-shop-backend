package com.threadcity.jacketshopbackend.repository;
public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {

    boolean existsByProductIdAndUserIdAndOrderId(
            Long productId, Long userId, Long orderId);

    Page<ProductReview> findByProductIdAndStatus(
            Long productId, ReviewStatus status, Pageable pageable);
}

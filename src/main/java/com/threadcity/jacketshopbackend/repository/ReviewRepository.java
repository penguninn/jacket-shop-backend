package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    List<Review> findByUserId(Long userId);

    // 🔍 Search + Filter cho Admin
    @Query("""
        SELECT r FROM Review r
        WHERE (:keyword IS NULL 
               OR LOWER(r.productName) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(r.userName) LIKE LOWER(CONCAT('%', :keyword, '%')))
          AND (:rating IS NULL OR r.rating = :rating)
    """)
    Page<Review> searchReviews(
            @Param("keyword") String keyword,
            @Param("rating") Integer rating,
            Pageable pageable
    );
}


package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    Optional<Product> findByName(String name);

    boolean existsByName(String name);

    @Modifying
    @Query("""
                UPDATE Product p
                SET p.soldCount = p.soldCount + :quantity
                WHERE p.id = :productId
            """)

    void increaseSoldCount(
            @Param("productId") Long productId,
            @Param("quantity") int quantity);

    // ============ STATISTICS QUERIES ============

    /**
     * Find top selling products ordered by sold count
     */
    @Query("""
            SELECT p.id, p.name, p.thumbnail, b.name, p.soldCount
            FROM Product p
            LEFT JOIN p.brand b
            WHERE p.status = 'ACTIVE'
            ORDER BY p.soldCount DESC
            """)
    List<Object[]> findTopSellingProducts(Pageable pageable);

    /**
     * Find top rated products ordered by rating average
     */
    @Query("""
            SELECT p.id, p.name, p.thumbnail, b.name, p.ratingAverage, p.ratingCount
            FROM Product p
            LEFT JOIN p.brand b
            WHERE p.status = 'ACTIVE' AND p.ratingCount > 0
            ORDER BY p.ratingAverage DESC, p.ratingCount DESC
            """)
    List<Object[]> findTopRatedProducts(Pageable pageable);

}
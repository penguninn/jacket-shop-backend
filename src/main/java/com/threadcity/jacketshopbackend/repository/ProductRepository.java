package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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

}

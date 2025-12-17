package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.ProductVariant;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long>, JpaSpecificationExecutor<ProductVariant> {
    boolean existsBySku(String sku);

    List<ProductVariant> findAllByProductId(Long productId);
}

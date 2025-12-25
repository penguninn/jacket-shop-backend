package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository
        extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByName(String name);

    List<Product> findTop4ByStatusOrderByIdDesc(Enums.Status status);

    List<Product> findTop4ByStatusOrderBySoldCountDesc(Enums.Status status);

    List<Product> findTop4ByIsFeaturedTrueAndStatus(Enums.Status status);

    List<Product> findTop2ByBrandIdAndIdNotAndStatusOrderByIdDesc(Long brandId, Long excludeId, Enums.Status status);

    List<Product> findTop2ByStyleIdAndIdNotAndStatusOrderByIdDesc(Long styleId, Long excludeId, Enums.Status status);
}



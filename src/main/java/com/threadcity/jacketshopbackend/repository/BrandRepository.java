package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long>, JpaSpecificationExecutor<Brand> {
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

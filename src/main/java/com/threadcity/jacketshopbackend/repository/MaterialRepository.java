package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Category;
import com.threadcity.jacketshopbackend.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> , JpaSpecificationExecutor<Material> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

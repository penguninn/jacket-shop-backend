package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MaterialRepository extends JpaRepository<Material, Long> {
    boolean existsByName(String name);
}

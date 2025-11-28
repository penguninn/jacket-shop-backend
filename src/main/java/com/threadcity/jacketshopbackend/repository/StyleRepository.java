package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface StyleRepository extends JpaRepository<Style, Long> , JpaSpecificationExecutor<Style> {
    boolean existsByName(String name);
}

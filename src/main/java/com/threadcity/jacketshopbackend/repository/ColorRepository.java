package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ColorRepository extends JpaRepository<Color,Long>, JpaSpecificationExecutor<Color> {
    boolean existsByName(String name);
}

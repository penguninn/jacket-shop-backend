package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Material;
import com.threadcity.jacketshopbackend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;


public interface SizeRepository extends JpaRepository<Size,Long>, JpaSpecificationExecutor<Size> {
    boolean existsByName(String name);
}

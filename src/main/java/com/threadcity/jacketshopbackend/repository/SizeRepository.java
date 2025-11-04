package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;


public interface SizeRepository extends JpaRepository<Size,Long> {
    boolean existsByName(String name);
}

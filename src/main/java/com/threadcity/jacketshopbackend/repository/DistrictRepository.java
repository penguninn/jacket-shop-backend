package com.threadcity.jacketshopbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.threadcity.jacketshopbackend.entity.District;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    
    List<District> findAllByProvinceId(Long provinceId);
}

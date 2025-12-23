package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

    List<District> findAllByProvinceId(Long provinceId);

    Optional<District> findByGoshipId(String goshipId);
}

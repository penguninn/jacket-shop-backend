package com.threadcity.jacketshopbackend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.threadcity.jacketshopbackend.entity.Ward;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    List<Ward> findAllByDistrictId(Long districtId);
}

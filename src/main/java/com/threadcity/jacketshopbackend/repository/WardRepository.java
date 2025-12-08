package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Ward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<Ward, Long> {

    List<Ward> findAllByDistrictId(Long districtId);
}

package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

    Optional<Province> findByGoshipId(String goshipId);
}

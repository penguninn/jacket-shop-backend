package com.threadcity.jacketshopbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.threadcity.jacketshopbackend.entity.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

}

package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Province;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

}

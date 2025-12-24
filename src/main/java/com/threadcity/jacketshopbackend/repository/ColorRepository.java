package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Color;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ColorRepository extends JpaRepository<Color, Long>, JpaSpecificationExecutor<Color> {
    Optional<Color> findByName(String name);
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Style;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StyleRepository extends JpaRepository<Style, Long>, JpaSpecificationExecutor<Style> {
    Optional<Style> findByName(String name);
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

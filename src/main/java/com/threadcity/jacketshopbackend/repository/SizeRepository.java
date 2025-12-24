package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SizeRepository extends JpaRepository<Size, Long>, JpaSpecificationExecutor<Size> {
    Optional<Size> findByName(String name);
    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.ShippingMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ShippingMethodsRepository extends JpaRepository<ShippingMethod, Long>, JpaSpecificationExecutor<ShippingMethod> {

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}

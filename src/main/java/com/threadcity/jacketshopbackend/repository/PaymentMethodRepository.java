package com.threadcity.jacketshopbackend.repository;

import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, Long>, JpaSpecificationExecutor<PaymentMethod> {

    boolean existsByName(String name);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByCode(String code);
    boolean existsByCodeAndIdNot(String code, Long id);

    Optional<PaymentMethod> findByCode(String code);
}

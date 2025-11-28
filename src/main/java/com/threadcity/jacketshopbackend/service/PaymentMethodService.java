package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.PaymentMethodFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.PaymentMethodRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.PaymentMethodResponse;
import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.PaymentMethodMapper;
import com.threadcity.jacketshopbackend.repository.PaymentMethodRepository;
import com.threadcity.jacketshopbackend.specification.PaymentMethodSpecification;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodService {

    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentMethodMapper paymentMethodMapper;

    public PaymentMethodResponse getPaymentMethodById(Long id) {
        log.info("PaymentMethodService::getPaymentMethodById - Execution started. [Id: {}]", id);

        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found with Id: " + id));

        log.info("PaymentMethodService::getPaymentMethodById - Execution completed. [Id: {}]", id);
        return paymentMethodMapper.toDto(method);
    }

    public PageResponse<?> getAllPaymentMethods(PaymentMethodFilterRequest request) {
        log.info("PaymentMethodService::getAllPaymentMethods - Execution started.");

        try {
            Sort sort = Sort.by(
                    Sort.Direction.fromString(request.getSortDir()),
                    request.getSortBy()
            );

            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Specification<PaymentMethod> spec = PaymentMethodSpecification.buildSpec(request);

            Page<PaymentMethod> page = paymentMethodRepository.findAll(spec, pageable);

            List<PaymentMethodResponse> responseList = page.getContent()
                    .stream()
                    .map(paymentMethodMapper::toDto)
                    .toList();

            log.info("PaymentMethodService::getAllPaymentMethods - Execution completed.");

            return PageResponse.builder()
                    .contents(responseList)
                    .size(request.getSize())
                    .page(request.getPage())
                    .totalPages(page.getTotalPages())
                    .totalElements(page.getTotalElements())
                    .build();

        } catch (Exception e) {
            log.error("PaymentMethodService::getAllPaymentMethods - Execution failed.", e);
            throw new BusinessException("PaymentMethodService::getAllPaymentMethods - Execution failed.");
        }
    }

    @Transactional
    public PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request) {
        log.info("PaymentMethodService::createPaymentMethod - Execution started.");

        if (paymentMethodRepository.existsByName(request.getName())) {
            throw new BusinessException("Payment method already exists with name: " + request.getName());
        }

        try {
            PaymentMethod entity = paymentMethodMapper.toEntity(request);
            PaymentMethod saved = paymentMethodRepository.save(entity);

            log.info("PaymentMethodService::createPaymentMethod - Execution completed.");
            return paymentMethodMapper.toDto(saved);

        } catch (Exception e) {
            log.error("PaymentMethodService::createPaymentMethod - Execution failed.", e);
            throw new BusinessException("PaymentMethodService::createPaymentMethod - Execution failed.");
        }
    }

    @Transactional
    public PaymentMethodResponse updatePaymentMethodById(PaymentMethodRequest request, Long id) {
        log.info("PaymentMethodService::updatePaymentMethodById - Execution started. [Id: {}]", id);

        PaymentMethod entity = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Payment method not found with Id: " + id));

        try {
            entity.setName(request.getName());
            entity.setDescription(request.getDescription());
            entity.setConfigJson(request.getConfigJson());
            entity.setStatus(request.getStatus());

            PaymentMethod saved = paymentMethodRepository.save(entity);

            log.info("PaymentMethodService::updatePaymentMethodById - Execution completed. [Id: {}]", id);
            return paymentMethodMapper.toDto(saved);

        } catch (RuntimeException e) {
            log.error("PaymentMethodService::updatePaymentMethodById - Execution failed.", e);
            throw new BusinessException("PaymentMethodService::updatePaymentMethodById - Execution failed.");
        }
    }

    @Transactional
    public void deletePaymentMethod(Long id) {
        log.info("PaymentMethodService::deletePaymentMethod - Execution started. [Id: {}]", id);

        if (!paymentMethodRepository.existsById(id)) {
            throw new EntityNotFoundException("Payment method not found with Id: " + id);
        }

        try {
            paymentMethodRepository.deleteById(id);
            log.info("PaymentMethodService::deletePaymentMethod - Execution completed. [Id: {}]", id);

        } catch (Exception e) {
            log.error("PaymentMethodService::deletePaymentMethod - Execution failed.", e);
            throw new BusinessException("PaymentMethodService::deletePaymentMethod - Execution failed.");
        }
    }
}

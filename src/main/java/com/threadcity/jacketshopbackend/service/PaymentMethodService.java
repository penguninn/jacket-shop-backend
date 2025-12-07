package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.PaymentMethodFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.PaymentMethodRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.PaymentMethodResponse;
import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.PaymentMethodMapper;
import com.threadcity.jacketshopbackend.repository.PaymentMethodRepository;
import com.threadcity.jacketshopbackend.specification.PaymentMethodSpecification;

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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PAYMENT_METHOD_NOT_FOUND,
                        "Payment method not found with Id: " + id));

        log.info("PaymentMethodService::getPaymentMethodById - Execution completed. [Id: {}]", id);
        return paymentMethodMapper.toDto(method);
    }

    public PageResponse<?> getAllPaymentMethods(PaymentMethodFilterRequest request) {
        log.info("PaymentMethodService::getAllPaymentMethods - Execution started.");

        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDir()),
                request.getSortBy());

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
    }

    @Transactional
    public PaymentMethodResponse createPaymentMethod(PaymentMethodRequest request) {
        log.info("PaymentMethodService::createPaymentMethod - Execution started.");

        if (paymentMethodRepository.existsByName(request.getName())) {
            throw new ResourceConflictException(ErrorCodes.PAYMENT_METHOD_NAME_DUPLICATE,
                    "Payment method already exists with name: " + request.getName());
        }

        PaymentMethod entity = paymentMethodMapper.toEntity(request);
        PaymentMethod saved = paymentMethodRepository.save(entity);

        log.info("PaymentMethodService::createPaymentMethod - Execution completed.");
        return paymentMethodMapper.toDto(saved);
    }

    @Transactional
    public PaymentMethodResponse updatePaymentMethodById(PaymentMethodRequest request, Long id) {
        log.info("PaymentMethodService::updatePaymentMethodById - Execution started. [Id: {}]", id);

        PaymentMethod entity = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PAYMENT_METHOD_NOT_FOUND,
                        "Payment method not found with Id: " + id));

        // Check for duplicate name if updating name
        if (!entity.getName().equals(request.getName()) && paymentMethodRepository.existsByName(request.getName())) {
            throw new ResourceConflictException(ErrorCodes.PAYMENT_METHOD_NAME_DUPLICATE,
                    "Payment method already exists with name: " + request.getName());
        }

        entity.setName(request.getName());
        entity.setDescription(request.getDescription());
        entity.setStatus(request.getStatus());

        PaymentMethod saved = paymentMethodRepository.save(entity);

        log.info("PaymentMethodService::updatePaymentMethodById - Execution completed. [Id: {}]", id);
        return paymentMethodMapper.toDto(saved);
    }

    @Transactional
    public void deletePaymentMethod(Long id) {
        log.info("PaymentMethodService::deletePaymentMethod - Execution started. [Id: {}]", id);

        if (!paymentMethodRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.PAYMENT_METHOD_NOT_FOUND,
                    "Payment method not found with Id: " + id);
        }

        paymentMethodRepository.deleteById(id);
        log.info("PaymentMethodService::deletePaymentMethod - Execution completed. [Id: {}]", id);
    }
}

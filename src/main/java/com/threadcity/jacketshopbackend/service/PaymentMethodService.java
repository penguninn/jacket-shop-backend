package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.PaymentMethodRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.PaymentMethodResponse;
import com.threadcity.jacketshopbackend.entity.PaymentMethod;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.filter.PaymentMethodFilterRequest;
import com.threadcity.jacketshopbackend.mapper.PaymentMethodMapper;
import com.threadcity.jacketshopbackend.repository.PaymentMethodRepository;
import com.threadcity.jacketshopbackend.specification.PaymentMethodSpecification;

import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
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

        if (paymentMethodRepository.existsByCode(request.getCode())) {
            throw new ResourceConflictException(ErrorCodes.PAYMENT_METHOD_CODE_DUPLICATE,
                    "Payment method already exists with code: " + request.getCode());
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

        if (paymentMethodRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ResourceConflictException(ErrorCodes.PAYMENT_METHOD_NAME_DUPLICATE,
                    "Payment method already exists with name: " + request.getName());
        }

        if (paymentMethodRepository.existsByCodeAndIdNot(request.getCode(), id)) {
            throw new ResourceConflictException(ErrorCodes.PAYMENT_METHOD_CODE_DUPLICATE,
                    "Payment method already exists with code: " + request.getCode());
        }

        entity.setName(request.getName());
        entity.setCode(request.getCode());
        entity.setType(request.getType());
        entity.setConfig(request.getConfig());
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

    @Transactional
    public PaymentMethodResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("PaymentMethodService::updateStatus - Execution started. [id: {}]", id);

        PaymentMethod method = paymentMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PAYMENT_METHOD_NOT_FOUND,
                        "Payment method not found with id: " + id));

        method.setStatus(request.getStatus());

        PaymentMethod saved = paymentMethodRepository.save(method);

        log.info("PaymentMethodService::updateStatus - Execution completed. [id: {}]", id);
        return paymentMethodMapper.toDto(saved);
    }

    @Transactional
    public List<PaymentMethodResponse> bulkUpdatePaymentMethodsStatus(BulkStatusRequest request) {
        log.info("PaymentMethodService::bulkUpdatePaymentMethodsStatus - Execution started.");

        List<PaymentMethod> methods = paymentMethodRepository.findAllById(request.getIds());
        if (methods.size() != request.getIds().size()) {
            Set<Long> foundIds = methods.stream().map(PaymentMethod::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.PAYMENT_METHOD_NOT_FOUND,
                    "Payment methods not found: " + missingIds);
        }

        methods.forEach(m -> m.setStatus(request.getStatus()));
        List<PaymentMethod> savedMethods = paymentMethodRepository.saveAll(methods);

        log.info("PaymentMethodService::bulkUpdatePaymentMethodsStatus - Execution completed.");
        return savedMethods.stream().map(paymentMethodMapper::toDto).toList();
    }

    @Transactional
    public void bulkDeletePaymentMethods(BulkDeleteRequest request) {
        log.info("PaymentMethodService::bulkDeletePaymentMethods - Execution started.");

        List<PaymentMethod> methods = paymentMethodRepository.findAllById(request.getIds());

        if (methods.size() != request.getIds().size()) {
            Set<Long> foundIds = methods.stream().map(PaymentMethod::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.PAYMENT_METHOD_NOT_FOUND,
                    "Payment methods not found: " + missingIds);
        }

        paymentMethodRepository.deleteAllInBatch(methods);

        log.info("PaymentMethodService::bulkDeletePaymentMethods - Execution completed.");
    }
}

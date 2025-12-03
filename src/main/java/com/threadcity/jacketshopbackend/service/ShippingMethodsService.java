package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.ShippingMethodsFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ShippingMethodsRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ShippingMethodsResponse;
import com.threadcity.jacketshopbackend.entity.ShippingMethod;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.ShippingMethodsMapper;
import com.threadcity.jacketshopbackend.repository.ShippingMethodsRepository;
import com.threadcity.jacketshopbackend.specification.ShippingMethodsSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingMethodsService {

    private final ShippingMethodsRepository shippingMethodRepository;
    private final ShippingMethodsMapper shippingMethodMapper;

    public ShippingMethodsResponse getShippingMethodById(Long id) {
        log.info("ShippingMethodService::getShippingMethodById - Execution started. [Id: {}]", id);

        ShippingMethod shippingMethod = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ShippingMethod not found with id: " + id));

        log.info("ShippingMethodService::getShippingMethodById - Execution completed. [Id: {}]", id);
        return shippingMethodMapper.toDto(shippingMethod);
    }

    public PageResponse<?> getAllShippingMethods(ShippingMethodsFilterRequest request) {
        log.info("ShippingMethodService::getAllShippingMethods - Execution started.");

        try {
            Sort sort = Sort.by(
                    Sort.Direction.fromString(request.getSortDir()),
                    request.getSortBy()
            );

            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
            Specification<ShippingMethod> spec = ShippingMethodsSpecification.buildSpec(request);

            Page<ShippingMethod> pageResult = shippingMethodRepository.findAll(spec, pageable);

            List<ShippingMethodsResponse> responseList = pageResult.getContent()
                    .stream()
                    .map(shippingMethodMapper::toDto)
                    .toList();

            log.info("ShippingMethodService::getAllShippingMethods - Execution completed.");

            return PageResponse.builder()
                    .contents(responseList)
                    .size(request.getSize())
                    .page(request.getPage())
                    .totalPages(pageResult.getTotalPages())
                    .totalElements(pageResult.getTotalElements())
                    .build();

        } catch (Exception e) {
            log.error("ShippingMethodService::getAllShippingMethods - Execution failed.", e);
            throw new BusinessException("ShippingMethodService::getAllShippingMethods - Execution failed.");
        }
    }

    @Transactional
    public ShippingMethodsResponse createShippingMethod(ShippingMethodsRequest request) {
        log.info("ShippingMethodService::createShippingMethod - Execution started.");

        if (shippingMethodRepository.existsByName(request.getName())) {
            throw new BusinessException("ShippingMethod already exists with name: " + request.getName());
        }

        try {
            ShippingMethod entity = shippingMethodMapper.toEntity(request);
            ShippingMethod saved = shippingMethodRepository.save(entity);

            log.info("ShippingMethodService::createShippingMethod - Execution completed.");
            return shippingMethodMapper.toDto(saved);

        } catch (Exception e) {
            log.error("ShippingMethodService::createShippingMethod - Execution failed.", e);
            throw new BusinessException("ShippingMethodService::createShippingMethod - Execution failed.");
        }
    }

    @Transactional
    public ShippingMethodsResponse updateShippingMethod(Long id, ShippingMethodsRequest request) {
        log.info("ShippingMethodService::updateShippingMethod - Execution started. [Id: {}]", id);

        ShippingMethod method = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("ShippingMethod not found with id: " + id));
        if (shippingMethodRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new BusinessException("Shipping method already exists with name: " + request.getName());
        }


        try {
            method.setName(request.getName());
            method.setDescription(request.getDescription());
            method.setFee(request.getFee());
            method.setEstimatedDays(request.getEstimatedDays());
            method.setStatus(request.getStatus());

            ShippingMethod saved = shippingMethodRepository.save(method);

            log.info("ShippingMethodService::updateShippingMethod - Execution completed. [Id: {}]", id);
            return shippingMethodMapper.toDto(saved);

        } catch (RuntimeException e) {
            log.error("ShippingMethodService::updateShippingMethod - Execution failed.", e);
            throw new BusinessException("ShippingMethodService::updateShippingMethod - Execution failed.");
        }
    }

    @Transactional
    public void deleteShippingMethod(Long id) {
        log.info("ShippingMethodService::deleteShippingMethod - Execution started. [Id: {}]", id);

        if (!shippingMethodRepository.existsById(id)) {
            throw new EntityNotFoundException("ShippingMethod not found with id: " + id);
        }

        try {
            shippingMethodRepository.deleteById(id);

            log.info("ShippingMethodService::deleteShippingMethod - Execution completed. [Id: {}]", id);

        } catch (Exception e) {
            log.error("ShippingMethodService::deleteShippingMethod - Execution failed.", e);
            throw new BusinessException("ShippingMethodService::deleteShippingMethod - Execution failed.");
        }
    }
    // =============================
    // BULK UPDATE STATUS
    // =============================
    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        log.info("ShippingMethodService::bulkUpdateStatus - Execution started.");

        List<ShippingMethod> methods = shippingMethodRepository.findAllById(ids);
        methods.forEach(m -> m.setStatus(
                Enum.valueOf(Enums.Status.class, status.toUpperCase())
        ));

        shippingMethodRepository.saveAll(methods);

        log.info("ShippingMethodService::bulkUpdateStatus - Execution completed.");
    }
    @Transactional
    public ShippingMethodsResponse updateStatus(Long id, String status) {
        log.info("ShippingMethodService::updateStatus - Execution started. [id: {}]", id);

        ShippingMethod method = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Shipping method not found with id: " + id));

        method.setStatus(Enum.valueOf(Enums.Status.class, status.toUpperCase()));

        ShippingMethod saved = shippingMethodRepository.save(method);

        log.info("ShippingMethodService::updateStatus - Execution completed. [id: {}]", id);

        return shippingMethodMapper.toDto(saved);
    }
    // =============================
    // BULK DELETE
    // =============================
    @Transactional
    public void bulkDelete(List<Long> ids) {
        log.info("ShippingMethodService::bulkDelete - Execution started.");

        List<ShippingMethod> methods = shippingMethodRepository.findAllById(ids);

        if (methods.size() != ids.size()) {
            throw new EntityNotFoundException("One or more shipping methods do not exist.");
        }

        shippingMethodRepository.deleteAllInBatch(methods);

        log.info("ShippingMethodService::bulkDelete - Execution completed.");
    }
}

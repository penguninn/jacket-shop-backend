package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ShippingMethodsFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ShippingMethodsRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ShippingMethodsResponse;
import com.threadcity.jacketshopbackend.entity.ShippingMethod;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ShippingMethodsMapper;
import com.threadcity.jacketshopbackend.repository.ShippingMethodsRepository;
import com.threadcity.jacketshopbackend.specification.ShippingMethodsSpecification;

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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SHIPPING_METHOD_NOT_FOUND,
                        "ShippingMethod not found with id: " + id));

        log.info("ShippingMethodService::getShippingMethodById - Execution completed. [Id: {}]", id);
        return shippingMethodMapper.toDto(shippingMethod);
    }

    public PageResponse<?> getAllShippingMethods(ShippingMethodsFilterRequest request) {
        log.info("ShippingMethodService::getAllShippingMethods - Execution started.");

        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDir()),
                request.getSortBy());

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
    }

    @Transactional
    public ShippingMethodsResponse createShippingMethod(ShippingMethodsRequest request) {
        log.info("ShippingMethodService::createShippingMethod - Execution started.");

        if (shippingMethodRepository.existsByName(request.getName())) {
            throw new ResourceConflictException(ErrorCodes.SHIPPING_METHOD_NAME_DUPLICATE,
                    "ShippingMethod already exists with name: " + request.getName());
        }

        ShippingMethod entity = shippingMethodMapper.toEntity(request);
        ShippingMethod saved = shippingMethodRepository.save(entity);

        log.info("ShippingMethodService::createShippingMethod - Execution completed.");
        return shippingMethodMapper.toDto(saved);
    }

    @Transactional
    public ShippingMethodsResponse updateShippingMethod(Long id, ShippingMethodsRequest request) {
        log.info("ShippingMethodService::updateShippingMethod - Execution started. [Id: {}]", id);

        ShippingMethod method = shippingMethodRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SHIPPING_METHOD_NOT_FOUND,
                        "ShippingMethod not found with id: " + id));

        // Check duplicate name
        if (!method.getName().equals(request.getName()) && shippingMethodRepository.existsByName(request.getName())) {
            throw new ResourceConflictException(ErrorCodes.SHIPPING_METHOD_NAME_DUPLICATE,
                    "ShippingMethod already exists with name: " + request.getName());
        }

        method.setName(request.getName());
        method.setDescription(request.getDescription());
        method.setFee(request.getFee());
        method.setEstimatedDays(request.getEstimatedDays());
        method.setStatus(request.getStatus());

        ShippingMethod saved = shippingMethodRepository.save(method);

        log.info("ShippingMethodService::updateShippingMethod - Execution completed. [Id: {}]", id);
        return shippingMethodMapper.toDto(saved);
    }

    @Transactional
    public void deleteShippingMethod(Long id) {
        log.info("ShippingMethodService::deleteShippingMethod - Execution started. [Id: {}]", id);

        if (!shippingMethodRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.SHIPPING_METHOD_NOT_FOUND,
                    "ShippingMethod not found with id: " + id);
        }

        shippingMethodRepository.deleteById(id);

        log.info("ShippingMethodService::deleteShippingMethod - Execution completed. [Id: {}]", id);
    }
}

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.SizeFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import com.threadcity.jacketshopbackend.entity.Size;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.SizeMapper;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
import com.threadcity.jacketshopbackend.specification.SizeSpecification;
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
public class SizeService {
    private final SizeRepository sizeRepository;
    private final SizeMapper sizeMapper;

    public SizeResponse getSizeById(Long Id) {
        log.info("SizeService::getSizeById - Execution started. [Id: {}]", Id);
        Size size = sizeRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND,
                        "Size not found with id: " + Id));
        log.info("SizeService::getSizeById - Execution completed. [SizeId: {}]", Id);
        return sizeMapper.toDto(size);
    }

    public PageResponse<?> getAllSizes(SizeFilterRequest request) {
        log.info("SizeService::getAllSizes - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Size> spec = SizeSpecification.buildSpec(request);
        Page<Size> sizePage = sizeRepository.findAll(spec, pageable);

        List<SizeResponse> sizeList = sizePage.getContent().stream()
                .map(sizeMapper::toDto)
                .toList();

        log.info("SizeService::getAllSizes - Execution completed.");
        return PageResponse.builder()
                .contents(sizeList)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(sizePage.getTotalPages())
                .totalElements(sizePage.getTotalElements())
                .build();
    }

    @Transactional
    public SizeResponse createSize(SizeRequest size) {
        log.info("SizeService::createSize- Execution started.");
        if (sizeRepository.existsByName(size.getName())) {
            throw new ResourceConflictException(ErrorCodes.SIZE_NAME_DUPLICATE,
                    "Size already exists with name: " + size.getName());
        }
        Size sizeEntity = sizeMapper.toEntity(size);
        Size saveSize = sizeRepository.save(sizeEntity);
        log.info("SizeService::createSize - Execution completed.");
        return sizeMapper.toDto(saveSize);
    }

    @Transactional
    public SizeResponse updateSizeById(SizeRequest sizeRequest, Long id) {
        log.info("SizeService::updateSizeById - Execution started.");

        Size size = sizeRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Size not found with id: " + id));

        size.setName(sizeRequest.getName());
        size.setDescription(sizeRequest.getDescription());
        size.setStatus(sizeRequest.getStatus());
        Size saveSize = sizeRepository.save(size);
        log.info("SizeService::updateProfile - Execution completed. [SizeId: {}]", id);
        return sizeMapper.toDto(saveSize);
    }

    @Transactional
    public void deleteSize(Long id) {
        log.info("SizeService::deleteSize - Execution started.");

        if (!sizeRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Size not found with id: " + id);
        }
        sizeRepository.deleteById(id);
        log.info("SizeService::deleteSize - Execution completed.");
    }

}

package com.threadcity.jacketshopbackend.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import com.threadcity.jacketshopbackend.entity.Size;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.SizeFilterRequest;
import com.threadcity.jacketshopbackend.mapper.SizeMapper;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
import com.threadcity.jacketshopbackend.specification.SizeSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        if (sizeRepository.existsByNameAndIdNot(sizeRequest.getName(), id)) {
            throw new ResourceConflictException(ErrorCodes.SIZE_NAME_DUPLICATE,
                    "Size already exists with name: " + sizeRequest.getName());
        }

        size.setName(sizeRequest.getName());
        size.setDescription(sizeRequest.getDescription());
        size.setStatus(sizeRequest.getStatus());
        Size saveSize = sizeRepository.save(size);
        log.info("SizeService::updateProfile - Execution completed. [SizeId: {}]", id);
        return sizeMapper.toDto(saveSize);
    }

    @Transactional
    public SizeResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("SizeService::updateStatus - Execution started. [Id: {}]", id);
        Size size = sizeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND,
                        "Size not found with id: " + id));
        size.setStatus(request.getStatus());
        Size savedSize = sizeRepository.save(size);
        log.info("SizeService::updateStatus - Execution completed. [Id: {}]", id);
        return sizeMapper.toDto(savedSize);
    }

    @Transactional
    public List<SizeResponse> bulkUpdateStatus(BulkStatusRequest request) {
        log.info("SizeService::bulkUpdateStatus - Execution started.");
        List<Size> sizes = sizeRepository.findAllById(request.getIds());
        if (sizes.size() != request.getIds().size()) {
            Set<Long> foundIds = sizes.stream().map(Size::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Sizes not found: " + missingIds);
        }
        sizes.forEach(size -> size.setStatus(request.getStatus()));
        List<Size> savedSizes = sizeRepository.saveAll(sizes);
        log.info("SizeService::bulkUpdateStatus - Execution completed.");
        return savedSizes.stream().map(sizeMapper::toDto).toList();
    }

    @Transactional
    public void bulkDelete(BulkDeleteRequest request) {
        log.info("SizeService::bulkDelete - Execution started.");
        List<Size> sizes = sizeRepository.findAllById(request.getIds());
        if (sizes.size() != request.getIds().size()) {
            Set<Long> foundIds = sizes.stream().map(Size::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.SIZE_NOT_FOUND, "Sizes not found: " + missingIds);
        }
        sizeRepository.deleteAllInBatch(sizes);
        log.info("SizeService::bulkDelete - Execution completed.");
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

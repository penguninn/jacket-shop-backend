package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.entity.Size;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.SizeMapper;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                .orElseThrow(() -> new EntityNotFoundException("Size not found with SizeId: " + Id));
        log.info("SizeService::getSizeById - Execution completed. [SizeId: {}]", Id);
        return sizeMapper.toDto(size);
    }

    public PageResponse<?> getAllSize(int page, int size, String sortBy) {
        log.info("SizeService::getAllSize - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Size> sizePage = sizeRepository.findAll(pageable);
            List<SizeResponse> BrandList = sizePage.stream()
                    .map(sizeMapper::toDto)
                    .toList();
            log.info("SizeService::getAllSize - Execution completed.");
            return PageResponse.builder()
                    .contents(BrandList)
                    .size(size)
                    .page(p)
                    .totalPages(sizePage.getTotalPages())
                    .totalElements(sizePage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("SizeService::getAllSize - Execution failed.", e);
            throw new BusinessException("SizeService::getAllSize - Execution failed.");
        }
    }
    @Transactional
    public SizeResponse createSize(SizeRequest size) {
        log.info("SizeService::createSize- Execution started.");
        if (sizeRepository.existsByName(size.getName())) {
            throw new BusinessException("Size already exists with name: " + size.getName());
        }
        try {
            Size sizeEntity = sizeMapper.toEntity(size);
            Size saveSize = sizeRepository.save(sizeEntity);
            log.info("SizeService::createSize - Execution completed.");
            return sizeMapper.toDto(saveSize);
        } catch (Exception e) {
            log.error("SizeService::createSize - Execution failed.", e);
            throw new BusinessException("SizeService::createSize - Execution failed.");
        }
    }
    @Transactional
    public SizeResponse updateSizeById(SizeRequest sizeRequest, Long id) {
        log.info("SizeService::updateSizeById - Execution started.");
        try {
            Size size = sizeRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Size not found with SizeId: " + id));
            size.setName(sizeRequest.getName());
            size.setStatus(sizeRequest.getStatus());
            Size saveSize = sizeRepository.save(size);
            log.info("SizeService::updateProfile - Execution completed. [SizeId: {}]", id);
            return sizeMapper.toDto(saveSize);
        } catch (RuntimeException e) {
            log.error("SizeService::updateProfile - Execution failed.", e);
            throw new BusinessException("SizeService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteSize(Long id) {
        log.info("SizeService::deleteSize - Execution started.");
        try {
            if (!sizeRepository.existsById(id)) {
                throw new EntityNotFoundException("Size not found with SizeId: " + id);
            }
            sizeRepository.deleteById(id);
            log.info("SizeService::deleteSize - Execution completed.");
        } catch (Exception e) {
            log.error("SizeService::deleteSize - Execution failed.", e);
            throw new BusinessException("SizeService::deleteSize - Execution failed.");
        }
    }

}

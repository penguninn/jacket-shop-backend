package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.MaterialFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.MaterialRequest;
import com.threadcity.jacketshopbackend.dto.response.MaterialResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Material;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.MaterialMapper;
import com.threadcity.jacketshopbackend.repository.MaterialRepository;
import com.threadcity.jacketshopbackend.specification.MaterialSpecification;

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
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    public MaterialResponse getMaterialById(Long id) {
        log.info("MaterialService::getMaterialById - Execution started. [Id: {}]", id);
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with MaterialId: " + id));
        log.info("MaterialService::getMaterialById - Execution completed. [MaterialId: {}]", id);
        return materialMapper.toDto(material);
    }

    public PageResponse<?> getAllMaterials(MaterialFilterRequest request) {
        log.info("MaterialService::getAllMaterials - Execution started.");

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Specification<Material> spec = MaterialSpecification.buildSpec(request);
            Page<Material> materialPage = materialRepository.findAll(spec, pageable);

            List<MaterialResponse> materialList = materialPage.getContent().stream()
                    .map(materialMapper::toDto)
                    .toList();

            log.info("MaterialService::getAllMaterials - Execution completed.");
            return PageResponse.builder()
                    .contents(materialList)
                    .size(request.getSize())
                    .page(request.getPage())
                    .totalPages(materialPage.getTotalPages())
                    .totalElements(materialPage.getTotalElements())
                    .build();

        } catch (Exception e) {
            log.error("MaterialService::getAllMaterials - Execution failed.", e);
            throw new BusinessException("MaterialService::getAllMaterials - Execution failed.");
        }
    }

    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        log.info("MaterialService::createMaterial - Execution started.");

        if (materialRepository.existsByName(request.getName())) {
            throw new BusinessException("Material already exists with name: " + request.getName());
        }

        try {
            Material material = materialMapper.toEntity(request);
            Material savedMaterial = materialRepository.save(material);

            log.info("MaterialService::createMaterial - Execution completed.");
            return materialMapper.toDto(savedMaterial);

        } catch (Exception e) {
            log.error("MaterialService::createMaterial - Execution failed.", e);
            throw new BusinessException("MaterialService::createMaterial - Execution failed.");
        }
    }

    @Transactional
    public MaterialResponse updateMaterialById(MaterialRequest request, Long id) {
        log.info("MaterialService::updateMaterialById - Execution started.");

        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with MaterialId: " + id));

        try {
            material.setName(request.getName());
            material.setDescription(request.getDescription());
            material.setStatus(request.getStatus());

            Material savedMaterial = materialRepository.save(material);
            log.info("MaterialService::updateMaterialById - Execution completed. [MaterialId: {}]", id);
            return materialMapper.toDto(savedMaterial);

        } catch (RuntimeException e) {
            log.error("MaterialService::updateMaterialById - Execution failed.", e);
            throw new BusinessException("MaterialService::updateMaterialById - Execution failed.");
        }
    }

    @Transactional
    public void deleteMaterial(Long id) {
        log.info("MaterialService::deleteMaterial - Execution started. [MaterialId: {}]", id);

        if (!materialRepository.existsById(id)) {
            throw new EntityNotFoundException("Material not found with MaterialId: " + id);
        }

        try {
            materialRepository.deleteById(id);
            log.info("MaterialService::deleteMaterial - Execution completed. [MaterialId: {}]", id);
        } catch (Exception e) {
            log.error("MaterialService::deleteMaterial - Execution failed.", e);
            throw new BusinessException("MaterialService::deleteMaterial - Execution failed.");
        }
    }
}

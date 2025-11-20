package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.MaterialRequest;
import com.threadcity.jacketshopbackend.dto.response.MaterialResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Material;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.MaterialMapper;
import com.threadcity.jacketshopbackend.repository.MaterialRepository;
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
public class MaterialService {
    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    public MaterialResponse getMaterialById(Long Id) {
        log.info("MaterialService::getMaterialById - Execution started. [Id: {}]", Id);
        Material material = materialRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with MaterialId: " + Id));
        log.info("MaterialService::getMaterialById - Execution completed. [MaterialId: {}]", Id);
        return materialMapper.toDto(material);
    }

    public PageResponse<?> getAllMaterials(int page, int size, String sortBy) {
        log.info("MaterialService::getAllMaterial - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Material> materialPage = materialRepository.findAll(pageable);
            List<MaterialResponse> MaterialList = materialPage.stream()
                    .map(materialMapper::toDto)
                    .toList();
            log.info("MaterialService::getAllMaterial - Execution completed.");
            return PageResponse.builder()
                    .contents(MaterialList)
                    .size(size)
                    .page(p)
                    .totalPages(materialPage.getTotalPages())
                    .totalElements(materialPage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("MaterialService::getAllMaterial - Execution failed.", e);
            throw new BusinessException("MaterialService::getAllMaterial - Execution failed.");
        }
    }
    @Transactional
    public MaterialResponse createMaterial(MaterialRequest material) {
        log.info("MaterialService::createMaterial - Execution started.");
        if (materialRepository.existsByName(material.getName())) {
            throw new BusinessException("material already exists with name: " + material.getName());
        }
        try {
            Material materialEntity = materialMapper.toEntity(material);
            Material savedMaterial = materialRepository.save(materialEntity);
            log.info("MaterialService::createMaterial - Execution completed.");
            return materialMapper.toDto(savedMaterial);
        } catch (Exception e) {
            log.error("MaterialService::createMaterial - Execution failed.", e);
            throw new BusinessException("MaterialService::createMaterial - Execution failed.");
        }
    }
    @Transactional
    public MaterialResponse updateMaterialById(MaterialRequest materialRequest,Long id) {
        log.info("MaterialService::updateMaterialById - Execution started.");
        try {
            Material material = materialRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Material not found with MaterialId: " + id));
            material.setName(materialRequest.getName());
            material.setDescription(materialRequest.getDescription());
            material.setStatus(materialRequest.getStatus());
            Material savedMaterial = materialRepository.save(material);
            log.info("MaterialService::updateProfile - Execution completed. [MaterialId: {}]", id);
            return materialMapper.toDto(savedMaterial);
        } catch (RuntimeException e) {
            log.error("MaterialService::updateProfile - Execution failed.", e);
            throw new BusinessException("MaterialService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteMaterial(Long id) {
        log.info("MaterialService::deleteMaterial - Execution started.");
        try {
            if (!materialRepository.existsById(id)) {
                throw new EntityNotFoundException("Material not found with MaterialId: " + id);
            }
            materialRepository.deleteById(id);
            log.info("MaterialService::deleteMaterial - Execution completed.");
        } catch (Exception e) {
            log.error("MaterialService::deleteMaterial - Execution failed.");
            throw new BusinessException("MaterialService::deleteMaterial - Execution failed." );
        }
    }
}

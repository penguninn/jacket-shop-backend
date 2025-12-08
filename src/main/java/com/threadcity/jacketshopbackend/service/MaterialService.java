package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.MaterialRequest;
import com.threadcity.jacketshopbackend.dto.response.MaterialResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Material;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.filter.MaterialFilterRequest;
import com.threadcity.jacketshopbackend.mapper.MaterialMapper;
import com.threadcity.jacketshopbackend.repository.MaterialRepository;
import com.threadcity.jacketshopbackend.specification.MaterialSpecification;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final MaterialMapper materialMapper;

    public MaterialResponse getMaterialById(Long id) {
        log.info("MaterialService::getMaterialById - Execution started. [Id: {}]", id);
        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND,
                        "Material not found with id: " + id));
        log.info("MaterialService::getMaterialById - Execution completed. [MaterialId: {}]", id);
        return materialMapper.toDto(material);
    }

    public PageResponse<?> getAllMaterials(MaterialFilterRequest request) {
        log.info("MaterialService::getAllMaterials - Execution started.");

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
    }

    @Transactional
    public MaterialResponse createMaterial(MaterialRequest request) {
        log.info("MaterialService::createMaterial - Execution started.");

        if (materialRepository.existsByName(request.getName())) {
            throw new ResourceConflictException(ErrorCodes.MATERIAL_NAME_DUPLICATE,
                    "Material already exists with name: " + request.getName());
        }

        Material material = materialMapper.toEntity(request);
        Material savedMaterial = materialRepository.save(material);

        log.info("MaterialService::createMaterial - Execution completed.");
        return materialMapper.toDto(savedMaterial);
    }

    @Transactional
    public MaterialResponse updateMaterialById(MaterialRequest request, Long id) {
        log.info("MaterialService::updateMaterialById - Execution started.");

        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND,
                        "Material not found with id: " + id));
        if (materialRepository.existsByNameAndIdNot(request.getName(), id)) {
            throw new ResourceConflictException(ErrorCodes.MATERIAL_NAME_DUPLICATE,
                    "Material already exists with name: " + request.getName());
        }

        material.setName(request.getName());
        material.setDescription(request.getDescription());
        material.setStatus(request.getStatus());

        Material savedMaterial = materialRepository.save(material);
        log.info("MaterialService::updateMaterialById - Execution completed. [MaterialId: {}]", id);
        return materialMapper.toDto(savedMaterial);
    }

    @Transactional
    public void deleteMaterial(Long id) {
        log.info("MaterialService::deleteMaterial - Execution started. [MaterialId: {}]", id);

        if (!materialRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND, "Material not found with id: " + id);
        }

        materialRepository.deleteById(id);
        log.info("MaterialService::deleteMaterial - Execution completed. [MaterialId: {}]", id);
    }

    @Transactional
    public MaterialResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("MaterialService::updateStatus - Execution started. [id: {}]", id);

        Material material = materialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND,
                        "Material not found with id: " + id));

        material.setStatus(request.getStatus());
        Material updated = materialRepository.save(material);

        log.info("MaterialService::updateStatus - Execution completed. [id: {}]", id);
        return materialMapper.toDto(updated);
    }

    @Transactional
    public List<MaterialResponse> bulkUpdateMaterialsStatus(BulkStatusRequest request) {
        log.info("MaterialService::bulkUpdateMaterialsStatus - Execution started.");

        List<Material> materials = materialRepository.findAllById(request.getIds());
        if (materials.size() != request.getIds().size()) {
            Set<Long> foundIds = materials.stream().map(Material::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND, "Materials not found: " + missingIds);
        }

        materials.forEach(m -> m.setStatus(request.getStatus()));
        List<Material> savedMaterials = materialRepository.saveAll(materials);

        log.info("MaterialService::bulkUpdateMaterialsStatus - Execution completed.");
        return savedMaterials.stream().map(materialMapper::toDto).toList();
    }

    @Transactional
    public void bulkDeleteMaterials(BulkDeleteRequest request) {
        log.info("MaterialService::bulkDeleteMaterials - Execution started.");

        List<Material> materials = materialRepository.findAllById(request.getIds());

        if (materials.size() != request.getIds().size()) {
            Set<Long> foundIds = materials.stream().map(Material::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND, "Materials not found: " + missingIds);
        }

        materialRepository.deleteAllInBatch(materials);

        log.info("MaterialService::bulkDeleteMaterials - Execution completed.");
    }
}

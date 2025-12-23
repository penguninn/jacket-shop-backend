package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.MaterialRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.MaterialResponse;
import com.threadcity.jacketshopbackend.entity.Material;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.MaterialMapper;
import com.threadcity.jacketshopbackend.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MaterialServiceTest {

    @Mock
    private MaterialRepository materialRepository;

    @Mock
    private MaterialMapper materialMapper;

    @InjectMocks
    private MaterialService materialService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetMaterialById_Found() {
        Material material = new Material();
        material.setId(1L);
        material.setName("Vải");
        material.setStatus(Enums.Status.ACTIVE);

        MaterialResponse response = new MaterialResponse();
        response.setName("Vải");
        response.setStatus(Enums.Status.ACTIVE);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialMapper.toDto(material)).thenReturn(response);

        MaterialResponse result = materialService.getMaterialById(1L);

        assertNotNull(result);
        assertEquals("Vải", result.getName());
    }

    @Test
    void testGetMaterialById_NotFound() {
        when(materialRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> materialService.getMaterialById(1L));
    }

    @Test
    void testCreateMaterial_Success() {
        MaterialRequest request = MaterialRequest.builder()
                .name("Vải")
                .description("Vải cotton")
                .status(Enums.Status.ACTIVE)
                .build();

        Material material = new Material();
        material.setName("Vải");
        material.setStatus(Enums.Status.ACTIVE);

        MaterialResponse response = new MaterialResponse();
        response.setName("Vải");

        when(materialRepository.existsByName("Vải")).thenReturn(false);
        when(materialMapper.toEntity(request)).thenReturn(material);
        when(materialRepository.save(material)).thenReturn(material);
        when(materialMapper.toDto(material)).thenReturn(response);

        MaterialResponse result = materialService.createMaterial(request);

        assertNotNull(result);
        assertEquals("Vải", result.getName());
    }

    @Test
    void testCreateMaterial_Conflict() {
        MaterialRequest request = MaterialRequest.builder()
                .name("Vải")
                .description("Vải cotton")
                .status(Enums.Status.ACTIVE)
                .build();

        when(materialRepository.existsByName("Vải")).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> materialService.createMaterial(request));
    }

    @Test
    void testUpdateMaterialById_Success() {
        MaterialRequest request = MaterialRequest.builder()
                .name("Vải Updated")
                .description("Vải cotton tốt")
                .status(Enums.Status.INACTIVE)
                .build();

        Material material = new Material();
        material.setId(1L);
        material.setName("Vải");
        material.setStatus(Enums.Status.ACTIVE);

        MaterialResponse response = new MaterialResponse();
        response.setName("Vải Updated");

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialRepository.existsByNameAndIdNot("Vải Updated", 1L)).thenReturn(false);
        when(materialRepository.save(material)).thenReturn(material);
        when(materialMapper.toDto(material)).thenReturn(response);

        MaterialResponse result = materialService.updateMaterialById(request, 1L);

        assertEquals("Vải Updated", result.getName());
    }

    @Test
    void testUpdateMaterialById_NotFound() {
        MaterialRequest request = MaterialRequest.builder()
                .name("Vải Updated")
                .status(Enums.Status.INACTIVE)
                .build();
        when(materialRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> materialService.updateMaterialById(request, 1L));
    }

    @Test
    void testDeleteMaterial_Success() {
        when(materialRepository.existsById(1L)).thenReturn(true);
        doNothing().when(materialRepository).deleteById(1L);
        assertDoesNotThrow(() -> materialService.deleteMaterial(1L));
    }

    @Test
    void testDeleteMaterial_NotFound() {
        when(materialRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> materialService.deleteMaterial(1L));
    }

    @Test
    void testUpdateStatus_Success() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(Enums.Status.INACTIVE);

        Material material = new Material();
        material.setId(1L);
        material.setStatus(Enums.Status.ACTIVE);

        MaterialResponse response = new MaterialResponse();
        response.setStatus(Enums.Status.INACTIVE);

        when(materialRepository.findById(1L)).thenReturn(Optional.of(material));
        when(materialRepository.save(material)).thenReturn(material);
        when(materialMapper.toDto(material)).thenReturn(response);

        MaterialResponse result = materialService.updateStatus(request, 1L);

        assertEquals(Enums.Status.INACTIVE, result.getStatus());
    }

    @Test
    void testBulkUpdateMaterialsStatus_Success() {
        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setStatus(Enums.Status.ACTIVE);

        Material m1 = new Material(); m1.setId(1L);
        Material m2 = new Material(); m2.setId(2L);

        MaterialResponse r1 = new MaterialResponse(); r1.setStatus(Enums.Status.ACTIVE);
        MaterialResponse r2 = new MaterialResponse(); r2.setStatus(Enums.Status.ACTIVE);

        when(materialRepository.findAllById(request.getIds())).thenReturn(Arrays.asList(m1, m2));
        when(materialRepository.saveAll(Arrays.asList(m1, m2))).thenReturn(Arrays.asList(m1, m2));
        when(materialMapper.toDto(m1)).thenReturn(r1);
        when(materialMapper.toDto(m2)).thenReturn(r2);

        List<MaterialResponse> result = materialService.bulkUpdateMaterialsStatus(request);

        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(r -> r.getStatus() == Enums.Status.ACTIVE));
    }

    @Test
    void testBulkUpdateMaterialsStatus_NotFound() {
        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(Arrays.asList(1L, 2L));
        request.setStatus(Enums.Status.ACTIVE);

        when(materialRepository.findAllById(request.getIds())).thenReturn(Collections.singletonList(new Material()));
        assertThrows(ResourceNotFoundException.class, () -> materialService.bulkUpdateMaterialsStatus(request));
    }

    @Test
    void testBulkDeleteMaterials_Success() {
        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(Arrays.asList(1L, 2L));

        Material m1 = new Material(); m1.setId(1L);
        Material m2 = new Material(); m2.setId(2L);

        when(materialRepository.findAllById(request.getIds())).thenReturn(Arrays.asList(m1, m2));
        doNothing().when(materialRepository).deleteAllInBatch(Arrays.asList(m1, m2));

        assertDoesNotThrow(() -> materialService.bulkDeleteMaterials(request));
    }

    @Test
    void testBulkDeleteMaterials_NotFound() {
        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(Arrays.asList(1L, 2L));

        when(materialRepository.findAllById(request.getIds())).thenReturn(Collections.singletonList(new Material()));
        assertThrows(ResourceNotFoundException.class, () -> materialService.bulkDeleteMaterials(request));
    }
}

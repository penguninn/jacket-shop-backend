package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.BrandRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.BrandResponse;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.BrandMapper;
import com.threadcity.jacketshopbackend.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @Mock
    private BrandMapper brandMapper;

    @InjectMocks
    private BrandService brandService;

    /* ===================== getBrandById ===================== */

    @Test
    void getBrandById_found_shouldReturnResponse() {
        Brand brand = new Brand();
        brand.setId(1L);

        BrandResponse response = new BrandResponse();

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandMapper.toDto(brand)).thenReturn(response);

        BrandResponse result = brandService.getBrandById(1L);

        assertNotNull(result);
        verify(brandRepository).findById(1L);
        verify(brandMapper).toDto(brand);
    }

    @Test
    void getBrandById_notFound_shouldThrowException() {
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> brandService.getBrandById(1L));
    }

    /* ===================== createBrand ===================== */

    @Test
    void createBrand_success() {
        BrandRequest request = BrandRequest.builder()
                .name("Nike")
                .build();

        Brand entity = new Brand();
        Brand saved = new Brand();
        BrandResponse response = new BrandResponse();

        when(brandRepository.existsByName("Nike")).thenReturn(false);
        when(brandMapper.toEntity(request)).thenReturn(entity);
        when(brandRepository.save(entity)).thenReturn(saved);
        when(brandMapper.toDto(saved)).thenReturn(response);

        BrandResponse result = brandService.createBrand(request);

        assertNotNull(result);
        verify(brandRepository).save(entity);
    }

    @Test
    void createBrand_duplicateName_shouldThrowException() {
        BrandRequest request = BrandRequest.builder()
                .name("Nike")
                .build();

        when(brandRepository.existsByName("Nike")).thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> brandService.createBrand(request));
    }

    /* ===================== updateBrandById ===================== */

    @Test
    void updateBrand_success() {
        BrandRequest request = BrandRequest.builder()
                .name("Adidas")
                .status(Status.ACTIVE)
                .build();

        Brand brand = new Brand();
        brand.setId(1L);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.existsByNameAndIdNot("Adidas", 1L)).thenReturn(false);
        when(brandRepository.save(brand)).thenReturn(brand);
        when(brandMapper.toDto(brand)).thenReturn(new BrandResponse());

        BrandResponse result = brandService.updateBrandById(request, 1L);

        assertNotNull(result);
        assertEquals(Status.ACTIVE, brand.getStatus());
        verify(brandRepository).save(brand);
    }

    /* ===================== deleteBrand ===================== */

    @Test
    void deleteBrand_success() {
        when(brandRepository.existsById(1L)).thenReturn(true);

        brandService.deleteBrand(1L);

        verify(brandRepository).deleteById(1L);
    }

    @Test
    void deleteBrand_notFound_shouldThrowException() {
        when(brandRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> brandService.deleteBrand(1L));
    }

    /* ===================== updateStatus ===================== */

    @Test
    void updateStatus_success() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(Status.INACTIVE);

        Brand brand = new Brand();
        brand.setStatus(Status.ACTIVE);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(brandRepository.save(brand)).thenReturn(brand);
        when(brandMapper.toDto(brand)).thenReturn(new BrandResponse());

        BrandResponse result = brandService.updateStatus(request, 1L);

        assertNotNull(result);
        assertEquals(Status.INACTIVE, brand.getStatus());
    }

    /* ===================== bulkUpdateBrandsStatus ===================== */

    @Test
    void bulkUpdateStatus_success() {
        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(List.of(1L, 2L));
        request.setStatus(Status.INACTIVE);

        Brand b1 = new Brand(); b1.setId(1L); b1.setStatus(Status.ACTIVE);
        Brand b2 = new Brand(); b2.setId(2L); b2.setStatus(Status.ACTIVE);

        when(brandRepository.findAllById(request.getIds()))
                .thenReturn(List.of(b1, b2));
        when(brandRepository.saveAll(anyList()))
                .thenReturn(List.of(b1, b2));
        when(brandMapper.toDto(any())).thenReturn(new BrandResponse());

        List<BrandResponse> result =
                brandService.bulkUpdateBrandsStatus(request);

        assertEquals(2, result.size());
        assertEquals(Status.INACTIVE, b1.getStatus());
        assertEquals(Status.INACTIVE, b2.getStatus());
    }

    /* ===================== bulkDeleteBrands ===================== */

    @Test
    void bulkDelete_success() {
        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L, 2L));

        Brand b1 = new Brand(); b1.setId(1L);
        Brand b2 = new Brand(); b2.setId(2L);

        when(brandRepository.findAllById(request.getIds()))
                .thenReturn(List.of(b1, b2));

        brandService.bulkDeleteBrands(request);

        verify(brandRepository).deleteAllInBatch(List.of(b1, b2));
    }

    @Test
    void bulkDelete_missingId_shouldThrowException() {
        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L, 2L));

        Brand b1 = new Brand(); b1.setId(1L);

        when(brandRepository.findAllById(request.getIds()))
                .thenReturn(List.of(b1));

        assertThrows(ResourceNotFoundException.class,
                () -> brandService.bulkDeleteBrands(request));
    }
}

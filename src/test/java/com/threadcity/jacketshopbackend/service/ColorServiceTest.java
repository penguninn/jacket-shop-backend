package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.ColorResponse;
import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ColorMapper;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
import com.threadcity.jacketshopbackend.common.Enums.Status;

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
class ColorServiceTest {

    @Mock
    private ColorRepository colorRepository;

    @Mock
    private ColorMapper colorMapper;

    @InjectMocks
    private ColorService colorService;

    /* ===================== getColorById ===================== */

    @Test
    void getColorById_found_shouldReturnColorResponse() {
        Color color = new Color();
        color.setId(1L);

        ColorResponse response = new ColorResponse();

        when(colorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(colorMapper.toDto(color)).thenReturn(response);

        ColorResponse result = colorService.getColorById(1L);

        assertNotNull(result);
        verify(colorRepository).findById(1L);
        verify(colorMapper).toDto(color);
    }

    @Test
    void getColorById_notFound_shouldThrowException() {
        when(colorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> colorService.getColorById(1L));
    }

    /* ===================== createColor ===================== */

    @Test
    void createColor_success() {
        ColorRequest request = ColorRequest.builder()
                .name("Red")
                .build();

        Color entity = new Color();
        Color saved = new Color();
        ColorResponse response = new ColorResponse();

        when(colorRepository.existsByName("Red")).thenReturn(false);
        when(colorMapper.toEntity(request)).thenReturn(entity);
        when(colorRepository.save(entity)).thenReturn(saved);
        when(colorMapper.toDto(saved)).thenReturn(response);

        ColorResponse result = colorService.createColor(request);

        assertNotNull(result);
        verify(colorRepository).save(entity);
    }

    @Test
    void createColor_duplicateName_shouldThrowException() {
        ColorRequest request = ColorRequest.builder()
                .name("Red")
                .build();

        when(colorRepository.existsByName("Red")).thenReturn(true);

        assertThrows(ResourceConflictException.class,
                () -> colorService.createColor(request));
    }

    /* ===================== updateColorById ===================== */

    @Test
    void updateColor_success() {
        ColorRequest request = ColorRequest.builder()
                .name("Blue")
                .build();

        Color color = new Color();
        color.setId(1L);

        when(colorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(colorRepository.existsByNameAndIdNot("Blue", 1L)).thenReturn(false);
        when(colorRepository.save(color)).thenReturn(color);
        when(colorMapper.toDto(color)).thenReturn(new ColorResponse());

        ColorResponse result = colorService.updateColorById(request, 1L);

        assertNotNull(result);
        verify(colorRepository).save(color);
    }

    @Test
    void updateColor_notFound_shouldThrowException() {
        ColorRequest request = ColorRequest.builder()
                .name("Blue")
                .build();

        when(colorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> colorService.updateColorById(request, 1L));
    }

    /* ===================== updateStatus ===================== */

    @Test
    void updateStatus_success() {
        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(Status.ACTIVE);

        Color color = new Color();

        when(colorRepository.findById(1L)).thenReturn(Optional.of(color));
        when(colorRepository.save(color)).thenReturn(color);
        when(colorMapper.toDto(color)).thenReturn(new ColorResponse());

        ColorResponse result = colorService.updateStatus(request, 1L);

        assertNotNull(result);
        assertEquals(Status.ACTIVE, color.getStatus());
    }


    /* ===================== bulkUpdateStatus ===================== */

    @Test
    void bulkUpdateStatus_success() {
        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(List.of(1L, 2L));
        request.setStatus(Status.INACTIVE);

        Color c1 = new Color(); c1.setId(1L);
        Color c2 = new Color(); c2.setId(2L);

        when(colorRepository.findAllById(request.getIds()))
                .thenReturn(List.of(c1, c2));
        when(colorRepository.saveAll(anyList()))
                .thenReturn(List.of(c1, c2));
        when(colorMapper.toDto(any()))
                .thenReturn(new ColorResponse());

        List<ColorResponse> result = colorService.bulkUpdateStatus(request);

        assertEquals(2, result.size());
        assertEquals(Status.INACTIVE, c1.getStatus());
        assertEquals(Status.INACTIVE, c2.getStatus());
    }


    @Test
    void bulkUpdateStatus_missingId_shouldThrowException() {
        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(List.of(1L, 2L));
        request.setStatus(Status.ACTIVE);

        Color c1 = new Color();
        c1.setId(1L);

        when(colorRepository.findAllById(request.getIds()))
                .thenReturn(List.of(c1));

        assertThrows(ResourceNotFoundException.class,
                () -> colorService.bulkUpdateStatus(request));
    }


    /* ===================== bulkDelete ===================== */

    @Test
    void bulkDelete_success() {
        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L, 2L));

        Color c1 = new Color(); c1.setId(1L);
        Color c2 = new Color(); c2.setId(2L);

        when(colorRepository.findAllById(request.getIds()))
                .thenReturn(List.of(c1, c2));

        colorService.bulkDelete(request);

        verify(colorRepository).deleteAllInBatch(List.of(c1, c2));
    }

    @Test
    void bulkDelete_missingId_shouldThrowException() {
        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L, 2L));

        Color c1 = new Color(); c1.setId(1L);

        when(colorRepository.findAllById(request.getIds()))
                .thenReturn(List.of(c1));

        assertThrows(ResourceNotFoundException.class,
                () -> colorService.bulkDelete(request));
    }

    /* ===================== deleteColor ===================== */

    @Test
    void deleteColor_success() {
        when(colorRepository.existsById(1L)).thenReturn(true);

        colorService.deleteColor(1L);

        verify(colorRepository).deleteById(1L);
    }

    @Test
    void deleteColor_notFound_shouldThrowException() {
        when(colorRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> colorService.deleteColor(1L));
    }
}

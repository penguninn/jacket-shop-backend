package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
import com.threadcity.jacketshopbackend.dto.response.ColorResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.ColorMapper;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
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
public class ColorService {
    private final ColorRepository colorRepository;
    private final ColorMapper colorMapper;

    public ColorResponse getColorById(Long Id) {
        log.info("ColorService::getColorById - Execution started. [Id: {}]", Id);
        Color color = colorRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Color not found with ColorId: " + Id));
        log.info("ColorService::getColorById - Execution completed. [ColorId: {}]", Id);
        return colorMapper.toDto(color);
    }

    public PageResponse<?> getAllColor(int page, int size, String sortBy) {
        log.info("ColorService::getAllColor - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Color> colorPage = colorRepository.findAll(pageable);
            List<ColorResponse> ColorList = colorPage.stream()
                    .map(colorMapper::toDto)
                    .toList();
            log.info("ColorService::getAllColor - Execution completed.");
            return PageResponse.builder()
                    .contents(ColorList)
                    .size(size)
                    .page(p)
                    .totalPages(colorPage.getTotalPages())
                    .totalElements(colorPage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("ColorService::getAllColor - Execution failed.", e);
            throw new BusinessException("ColorService::getAllColor - Execution failed.");
        }
    }
    @Transactional
    public ColorResponse createColor(ColorRequest color) {
        log.info("ColorService::createColor - Execution started.");
        if (colorRepository.existsByName(color.getName())) {
            throw new BusinessException("Color already exists with name: " + color.getName());
        }
        try {
            Color colorEntity = colorMapper.toEntity(color);
            Color savedColor = colorRepository.save(colorEntity);
            log.info("ColorService::createColor - Execution completed.");
            return colorMapper.toDto(savedColor);
        } catch (Exception e) {
            log.error("ColorService::createColor - Execution failed.", e);
            throw new BusinessException("ColorService::createColor - Execution failed.");
        }
    }
    @Transactional
    public ColorResponse updateColorById(ColorRequest colorRequest, Long id) {
        log.info("ColorService::updateColorById - Execution started.");
        try {
            Color color = colorRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Color not found with ColorId: " + id));
            color.setName(colorRequest.getName());
            color.setStatus(colorRequest.getStatus());
            Color savedColor = colorRepository.save(color);
            log.info("ColorService::updateProfile - Execution completed. [ColorId: {}]", id);
            return colorMapper.toDto(savedColor);
        } catch (RuntimeException e) {
            log.error("ColorService::updateProfile - Execution failed.", e);
            throw new BusinessException("ColorService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteColor(Long id) {
        log.info("ColorService::deleteColor - Execution started.");
        try {
            if (!colorRepository.existsById(id)) {
                throw new EntityNotFoundException("Color not found with ColorId: " + id);
            }
            colorRepository.deleteById(id);
            log.info("ColorService::deleteColor - Execution completed.");
        } catch (Exception e) {
            log.error("ColorService::deleteColor - Execution failed.", e);
            throw new BusinessException("ColorService::deleteColor - Execution failed.");
        }
    }
}



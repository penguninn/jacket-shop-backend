package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ColorFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
import com.threadcity.jacketshopbackend.dto.response.ColorResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Color;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ColorMapper;
import com.threadcity.jacketshopbackend.repository.ColorRepository;
import com.threadcity.jacketshopbackend.specification.ColorSpecification;
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
public class ColorService {
    private final ColorRepository colorRepository;
    private final ColorMapper colorMapper;

    public ColorResponse getColorById(Long Id) {
        log.info("ColorService::getColorById - Execution started. [Id: {}]", Id);
        Color color = colorRepository.findById(Id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND,
                        "Color not found with id: " + Id));
        log.info("ColorService::getColorById - Execution completed. [ColorId: {}]", Id);
        return colorMapper.toDto(color);
    }

    public PageResponse<?> getAllColors(ColorFilterRequest request) {
        log.info("ColorService::getAllColors - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Color> spec = ColorSpecification.buildSpec(request);
        Page<Color> colorPage = colorRepository.findAll(spec, pageable);

        List<ColorResponse> colorList = colorPage.getContent().stream()
                .map(colorMapper::toDto)
                .toList();

        log.info("ColorService::getAllColors - Execution completed.");
        return PageResponse.builder()
                .contents(colorList)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(colorPage.getTotalPages())
                .totalElements(colorPage.getTotalElements())
                .build();
    }

    @Transactional
    public ColorResponse createColor(ColorRequest color) {
        log.info("ColorService::createColor - Execution started.");
        if (colorRepository.existsByName(color.getName())) {
            throw new ResourceConflictException(ErrorCodes.COLOR_NAME_DUPLICATE,
                    "Color already exists with name: " + color.getName());
        }

        Color colorEntity = colorMapper.toEntity(color);
        Color savedColor = colorRepository.save(colorEntity);
        log.info("ColorService::createColor - Execution completed.");
        return colorMapper.toDto(savedColor);
    }

    @Transactional
    public ColorResponse updateColorById(ColorRequest colorRequest, Long id) {
        log.info("ColorService::updateColorById - Execution started.");

        Color color = colorRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Color not found with id: " + id));
        color.setName(colorRequest.getName());
        color.setDescription(colorRequest.getDescription());
        color.setStatus(colorRequest.getStatus());
        Color savedColor = colorRepository.save(color);
        log.info("ColorService::updateProfile - Execution completed. [ColorId: {}]", id);
        return colorMapper.toDto(savedColor);
    }

    @Transactional
    public void deleteColor(Long id) {
        log.info("ColorService::deleteColor - Execution started.");

        if (!colorRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Color not found with id: " + id);
        }
        colorRepository.deleteById(id);
        log.info("ColorService::deleteColor - Execution completed.");
    }
}

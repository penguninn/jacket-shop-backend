package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.filter.ColorFilterRequest;

import com.threadcity.jacketshopbackend.dto.request.ColorRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
        if (colorRepository.existsByNameAndIdNot(colorRequest.getName(), id)) {
            throw new ResourceConflictException(ErrorCodes.COLOR_NAME_DUPLICATE,
                    "Color already exists with name: " + colorRequest.getName());
        }

        color.setName(colorRequest.getName());
        color.setDescription(colorRequest.getDescription());
        color.setHexCode(colorRequest.getHexCode());
        color.setStatus(colorRequest.getStatus());
        Color savedColor = colorRepository.save(color);
        log.info("ColorService::updateProfile - Execution completed. [ColorId: {}]", id);
        return colorMapper.toDto(savedColor);
    }

    @Transactional
    public ColorResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("ColorService::updateStatus - Execution started. [Id: {}]", id);
        Color color = colorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND,
                        "Color not found with id: " + id));
        color.setStatus(request.getStatus());
        Color savedColor = colorRepository.save(color);
        log.info("ColorService::updateStatus - Execution completed. [Id: {}]", id);
        return colorMapper.toDto(savedColor);
    }

    @Transactional
    public List<ColorResponse> bulkUpdateStatus(BulkStatusRequest request) {
        log.info("ColorService::bulkUpdateStatus - Execution started.");
        List<Color> colors = colorRepository.findAllById(request.getIds());
        if (colors.size() != request.getIds().size()) {
            Set<Long> foundIds = colors.stream().map(Color::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Colors not found: " + missingIds);
        }
        colors.forEach(color -> color.setStatus(request.getStatus()));
        List<Color> savedColors = colorRepository.saveAll(colors);
        log.info("ColorService::bulkUpdateStatus - Execution completed.");
        return savedColors.stream().map(colorMapper::toDto).toList();
    }

    @Transactional
    public void bulkDelete(BulkDeleteRequest request) {
        log.info("ColorService::bulkDelete - Execution started.");
        List<Color> colors = colorRepository.findAllById(request.getIds());
        if (colors.size() != request.getIds().size()) {
            Set<Long> foundIds = colors.stream().map(Color::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.COLOR_NOT_FOUND, "Colors not found: " + missingIds);
        }
        colorRepository.deleteAllInBatch(colors);
        log.info("ColorService::bulkDelete - Execution completed.");
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

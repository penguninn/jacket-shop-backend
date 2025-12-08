package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.filter.StyleFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.StyleRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.StyleResponse;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.StyleMapper;
import com.threadcity.jacketshopbackend.repository.StyleRepository;
import com.threadcity.jacketshopbackend.specification.StyleSpecification;
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
public class StyleService {
    private final StyleRepository styleRepository;
    private final StyleMapper styleMapper;

    public StyleResponse getStyleById(Long id) {
        log.info("StyleService::getStyleById - Execution started. [Id: {}]", id);
        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND,
                        "Style not found with id: " + id));
        log.info("StyleService::getStyleById - Execution completed. [StyleId: {}]", id);
        return styleMapper.toDto(style);
    }

    public PageResponse<?> getAllStyles(StyleFilterRequest request) {
        log.info("StyleService::getAllStyles - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Style> spec = StyleSpecification.buildSpec(request);
        Page<Style> stylePage = styleRepository.findAll(spec, pageable);

        List<StyleResponse> styleResponse = stylePage.getContent().stream()
                .map(styleMapper::toDto)
                .toList();
        log.info("StyleService::getAllStyles - Execution completed.");
        return PageResponse.builder()
                .contents(styleResponse)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(stylePage.getTotalPages())
                .totalElements(stylePage.getTotalElements())
                .build();
    }

    @Transactional
    public StyleResponse createStyle(StyleRequest style) {
        log.info("StyleService::createStyle - Execution started.");
        if (styleRepository.existsByName(style.getName())) {
            throw new ResourceConflictException(ErrorCodes.STYLE_NAME_DUPLICATE,
                    "Style already exists with name: " + style.getName());
        }

        Style styleEntity = styleMapper.toEntity(style);
        Style savedStyle = styleRepository.save(styleEntity);
        log.info("StyleService::createStyle - Execution completed.");
        return styleMapper.toDto(savedStyle);
    }

    @Transactional
    public StyleResponse updateStyleById(StyleRequest styleRequest, Long id) {
        log.info("StyleService::updateStyleById - Execution started.");

        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND,
                        "Style not found with id: " + id));

        if (styleRepository.existsByNameAndIdNot(styleRequest.getName(), id)) {
            throw new ResourceConflictException(ErrorCodes.STYLE_NAME_DUPLICATE,
                    "Style already exists with name: " + styleRequest.getName());
        }

        style.setName(styleRequest.getName());
        style.setDescription(styleRequest.getDescription());
        style.setStatus(styleRequest.getStatus());
        Style savedStyle = styleRepository.save(style);
        log.info("StyleService::updateProfile - Execution completed. [StyleId: {}]", id);
        return styleMapper.toDto(savedStyle);
    }

    @Transactional
    public void deleteStyle(Long id) {
        log.info("StyleService::deleteStyle - Execution started.");

        if (!styleRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND, "Style not found with id: " + id);
        }
        styleRepository.deleteById(id);
        log.info("StyleService::deleteStyle - Execution completed.");
    }

    @Transactional
    public StyleResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("StyleService::updateStatus - Execution started. [id: {}]", id);

        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND,
                        "Style not found with id: " + id));

        style.setStatus(request.getStatus());

        Style saved = styleRepository.save(style);

        log.info("StyleService::updateStatus - Execution completed. [id: {}]", id);

        return styleMapper.toDto(saved);
    }

    @Transactional
    public List<StyleResponse> bulkUpdateStylesStatus(BulkStatusRequest request) {
        log.info("StyleService::bulkUpdateStylesStatus - Execution started.");

        List<Style> styles = styleRepository.findAllById(request.getIds());
        if (styles.size() != request.getIds().size()) {
            Set<Long> foundIds = styles.stream().map(Style::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND, "Styles not found: " + missingIds);
        }

        styles.forEach(s -> s.setStatus(request.getStatus()));

        List<Style> savedStyles = styleRepository.saveAll(styles);

        log.info("StyleService::bulkUpdateStylesStatus - Execution completed.");
        return savedStyles.stream().map(styleMapper::toDto).toList();
    }

    @Transactional
    public void bulkDeleteStyles(BulkDeleteRequest request) {
        log.info("StyleService::bulkDeleteStyles - Execution started.");

        List<Style> styles = styleRepository.findAllById(request.getIds());

        if (styles.size() != request.getIds().size()) {
            Set<Long> foundIds = styles.stream().map(Style::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND, "Styles not found: " + missingIds);
        }

        styleRepository.deleteAllInBatch(styles);

        log.info("StyleService::bulkDeleteStyles - Execution completed.");
    }
}

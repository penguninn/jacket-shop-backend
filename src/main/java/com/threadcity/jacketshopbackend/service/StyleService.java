package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.StyleFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.StyleRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.StyleResponse;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.StyleMapper;
import com.threadcity.jacketshopbackend.repository.StyleRepository;
import com.threadcity.jacketshopbackend.specification.StyleSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class StyleService {
    private final StyleRepository styleRepository;
    private final StyleMapper styleMapper;

    public StyleResponse getStyleById(Long id) {
        log.info("StyleService::getStyleById - Execution started. [Id: {}]", id);
        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Style not found with StyleId: " + id));
        log.info("StyleService::getStyleById - Execution completed. [StyleId: {}]", id);
        return styleMapper.toDto(style);
    }

    public PageResponse<?> getAllStyle(StyleFilterRequest request) {
        log.info("StyleService::getAllStyle - Execution started.");
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Specification<Style> spec = StyleSpecification.buildSpec(request);
            Page<Style> stylePage = styleRepository.findAll(spec, pageable);

            List<StyleResponse> styleResponse = stylePage.getContent().stream()
                    .map(styleMapper::toDto)
                    .toList();
            log.info("StyleService::getAllStyle - Execution completed.");
            return PageResponse.builder()
                    .contents(styleResponse)
                    .size(request.getSize())
                    .page(request.getPage())
                    .totalPages(stylePage.getTotalPages())
                    .totalElements(stylePage.getTotalElements())
                    .build();

        } catch (Exception e) {
            log.error("StyleService::getAllStyle - Execution failed.", e);
            throw new BusinessException("Failed to get styles.");
        }
    }

    @Transactional
    public StyleResponse createStyle(StyleRequest style) {
        log.info("StyleService::createStyle - Execution started.");
        if (styleRepository.existsByName(style.getName())) {
            throw new BusinessException("Style already exists with name: " + style.getName());
        }
        try {
            Style styleEntity = styleMapper.toEntity(style);
            Style savedStyle = styleRepository.save(styleEntity);
            log.info("StyleService::createStyle - Execution completed.");
            return styleMapper.toDto(savedStyle);
        } catch (Exception e) {
            log.error("StyleService::createStyle - Execution failed.", e);
            throw new BusinessException("StyleService::createStyle - Execution failed.");
        }
    }
    @Transactional
    public StyleResponse updateStyleById(StyleRequest styleRequest, Long id) {
        log.info("StyleService::updateStyleById - Execution started.");
        try {
            Style style = styleRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Style not found with StyleId: " + id));
            style.setName(styleRequest.getName());
            style.setDescription(styleRequest.getDescription());
            style.setStatus(styleRequest.getStatus());
            Style savedStyle = styleRepository.save(style);
            log.info("StyleService::updateProfile - Execution completed. [StyleId: {}]", id);
            return styleMapper.toDto(savedStyle);
        } catch (RuntimeException e) {
            log.error("StyleService::updateProfile - Execution failed.", e);
            throw new BusinessException("StyleService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteStyle(Long id) {
        log.info("StyleService::deleteStyle - Execution started.");
        try {
            if (!styleRepository.existsById(id)) {
                throw new EntityNotFoundException("Style not found with StyleId: " + id);
            }
            styleRepository.deleteById(id);
            log.info("StyleService::deleteStyle - Execution completed.");
        } catch (Exception e) {
            log.error("StyleService::deleteStyle - Execution failed.", e);
            throw new BusinessException("StyleService::deleteStyle - Execution failed.");
        }
    }
    @Transactional
    public StyleResponse updateStatus(Long id, String status) {
        log.info("StyleService::updateStatus - Execution started. [id: {}]", id);

        Style style = styleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Style not found with id: " + id));

        style.setStatus(Enum.valueOf(com.threadcity.jacketshopbackend.common.Enums.Status.class, status.toUpperCase()));

        Style saved = styleRepository.save(style);

        log.info("StyleService::updateStatus - Execution completed. [id: {}]", id);

        return styleMapper.toDto(saved);
    }

    // =============================
    // BULK UPDATE STATUS
    // =============================
    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        log.info("StyleService::bulkUpdateStatus - Execution started.");

        List<Style> styles = styleRepository.findAllById(ids);
        styles.forEach(s -> s.setStatus(Enum.valueOf(com.threadcity.jacketshopbackend.common.Enums.Status.class, status.toUpperCase())));

        styleRepository.saveAll(styles);

        log.info("StyleService::bulkUpdateStatus - Execution completed.");
    }

    // =============================
    // BULK DELETE
    // =============================
    @Transactional
    public void bulkDelete(List<Long> ids) {
        log.info("StyleService::bulkDelete - Execution started.");

        List<Style> styles = styleRepository.findAllById(ids);

        if (styles.size() != ids.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Một hoặc nhiều style không tồn tại.");
        }

        styleRepository.deleteAllInBatch(styles);

        log.info("StyleService::bulkDelete - Execution completed.");
    }
}

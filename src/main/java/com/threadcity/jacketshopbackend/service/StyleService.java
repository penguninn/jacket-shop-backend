package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.StyleRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.StyleResponse;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.StyleMapper;
import com.threadcity.jacketshopbackend.repository.StyleRepository;
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
public class StyleService {
    private final StyleRepository styleRepository;
    private final StyleMapper styleMapper;

    public StyleResponse getStyleById(Integer Id) {
        log.info("StyleService::getStyleById - Execution started. [Id: {}]", Id);
        Style style = styleRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Style not found with StyleId: " + Id));
        log.info("StyleService::getStyleById - Execution completed. [StyleId: {}]", Id);
        return styleMapper.toDto(style);
    }

    public PageResponse<?> getAllStyle(int page, int size, String sortBy) {
        log.info("StyleService::getAllStyle - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Style> stylePage = styleRepository.findAll(pageable);
            List<StyleResponse> StyleList = stylePage.stream()
                    .map(styleMapper::toDto)
                    .toList();
            log.info("StyleService::getAllStyle - Execution completed.");
            return PageResponse.builder()
                    .contents(StyleList)
                    .size(size)
                    .page(p)
                    .totalPages(stylePage.getTotalPages())
                    .totalElements(stylePage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("StyleService::getAllStyle - Execution failed.", e);
            throw new BusinessException("StyleService::getAllStyle - Execution failed.");
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
    public StyleResponse updateStyleById(StyleRequest styleRequest, Integer id) {
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
    public void deleteStyle(Integer id) {
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
}

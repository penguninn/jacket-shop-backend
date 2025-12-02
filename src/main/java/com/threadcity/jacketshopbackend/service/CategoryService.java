package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.CategoryFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.CategoryRequest;
import com.threadcity.jacketshopbackend.dto.response.CategoryResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Category;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.CategoryMapper;
import com.threadcity.jacketshopbackend.repository.CategoryRepository;
import com.threadcity.jacketshopbackend.specification.CategorySpecification;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse getCategoryById(Long id) {
        log.info("CategoryService::getCategoryById - Execution started. [Id: {}]", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with CategoryId: " + id));

        log.info("CategoryService::getCategoryById - Execution completed. [CategoryId: {}]", id);
        return categoryMapper.toDto(category);
    }
    public PageResponse<?> getAllCategories(CategoryFilterRequest request) {
        log.info("CategoryService::getAllCategories - Execution started.");

        try {
            Sort sort = Sort.by(
                    Sort.Direction.fromString(request.getSortDir()),
                    request.getSortBy()
            );
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Specification<Category> spec = CategorySpecification.buildSpec(request);

            Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

            List<CategoryResponse> responseList = categoryPage.getContent()
                    .stream()
                    .map(categoryMapper::toDto)
                    .toList();

            log.info("CategoryService::getAllCategories - Execution completed.");

            return PageResponse.builder()
                    .contents(responseList)
                    .size(request.getSize())
                    .page(request.getPage())
                    .totalPages(categoryPage.getTotalPages())
                    .totalElements(categoryPage.getTotalElements())
                    .build();

        } catch (Exception e) {
            log.error("CategoryService::getAllCategories - Execution failed.", e);
            throw new BusinessException("CategoryService::getAllCategories - Execution failed.");
        }
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("CategoryService::createCategory - Execution started.");

        if (categoryRepository.existsByName(request.getName())) {
            throw new BusinessException("Category already exists with name: " + request.getName());
        }

        try {
            Category category = categoryMapper.toEntity(request);
            Category savedCategory = categoryRepository.save(category);

            log.info("CategoryService::createCategory - Execution completed.");
            return categoryMapper.toDto(savedCategory);

        } catch (Exception e) {
            log.error("CategoryService::createCategory - Execution failed.", e);
            throw new BusinessException("CategoryService::createCategory - Execution failed.");
        }
    }

    @Transactional
    public CategoryResponse updateCategoryById(CategoryRequest request, Long id) {
        log.info("CategoryService::updateCategoryById - Execution started.");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with CategoryId: " + id));

        try {
            category.setName(request.getName());
            category.setStatus(request.getStatus());

            Category savedCategory = categoryRepository.save(category);

            log.info("CategoryService::updateCategoryById - Execution completed. [CategoryId: {}]", id);
            return categoryMapper.toDto(savedCategory);

        } catch (RuntimeException e) {
            log.error("CategoryService::updateCategoryById - Execution failed.", e);
            throw new BusinessException("CategoryService::updateCategoryById - Execution failed.");
        }
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("CategoryService::deleteCategory - Execution started. [CategoryId: {}]", id);

        if (!categoryRepository.existsById(id)) {
            throw new EntityNotFoundException("Category not found with CategoryId: " + id);
        }

        try {
            categoryRepository.deleteById(id);
            log.info("CategoryService::deleteCategory - Execution completed. [CategoryId: {}]", id);

        } catch (Exception e) {
            log.error("CategoryService::deleteCategory - Execution failed.", e);
            throw new BusinessException("CategoryService::deleteCategory - Execution failed.");
        }
    }

    @Transactional
    public CategoryResponse updateCategoryStatus(Long id, Enums.Status status) {
        log.info("CategoryService::updateCategoryStatus - Execution started. [CategoryId: {}]", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with CategoryId: " + id));

        try {
            category.setStatus(status);
            Category updated = categoryRepository.save(category);

            log.info("CategoryService::updateCategoryStatus - Execution completed. [CategoryId: {}]", id);
            return categoryMapper.toDto(updated);

        } catch (Exception e) {
            log.error("CategoryService::updateCategoryStatus - Execution failed.", e);
            throw new BusinessException("CategoryService::updateCategoryStatus - Execution failed.");
        }
    }

}

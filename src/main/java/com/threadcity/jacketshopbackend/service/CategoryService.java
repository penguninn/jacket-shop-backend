package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.CategoryRequest;
import com.threadcity.jacketshopbackend.dto.response.CategoryResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Category;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.CategoryMapper;
import com.threadcity.jacketshopbackend.repository.CategoryRepository;
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
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse getCategoryById(Long Id) {
        log.info("CategoryService::getCategoryById - Execution started. [Id: {}]", Id);
        Category category =  categoryRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with CategoryId: " + Id));
        log.info("CategoryService::getCategoryById - Execution completed. [CategoryId: {}]", Id);
        return categoryMapper.toDto(category);
    }

    public PageResponse<?> getAllCategorys(int page, int size, String sortBy) {
        log.info("CategoryService::getAllCategory - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Category> categoryPage =  categoryRepository.findAll(pageable);
            List<CategoryResponse> CategoryList = categoryPage.stream()
                    .map(categoryMapper::toDto)
                    .toList();
            log.info("CategoryService::getAllCategory - Execution completed.");
            return PageResponse.builder()
                    .contents(CategoryList)
                    .size(size)
                    .page(p)
                    .totalPages(categoryPage.getTotalPages())
                    .totalElements(categoryPage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("CategoryService::getAllCategory  - Execution failed.", e);
            throw new BusinessException("CategoryService::getAllCategory - Execution failed.");
        }
    }
    @Transactional
    public CategoryResponse createCategory(CategoryRequest category) {
        log.info("CategoryService::createCategory - Execution started.");
        if ( categoryRepository.existsByName(category.getName())) {
            throw new BusinessException("Category already exists with name: " + category.getName());
        }
        try {
            Category categoryEntity = categoryMapper.toEntity(category);
            Category savedCategory =  categoryRepository.save(categoryEntity);
            log.info("CategoryService::createCategory - Execution completed.");
            return categoryMapper.toDto(savedCategory);
        } catch (Exception e) {
            log.error("CategoryService::createCategory - Execution failed.", e);
            throw new BusinessException("CategoryService::createCategory - Execution failed.");
        }
    }
    @Transactional
    public CategoryResponse updateCategoryById(CategoryRequest categoryRequest, Long id) {
        log.info("CategoryService::updateCategoryById - Execution started.");
        try {
            Category category =  categoryRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Category not found with CategoryId: " + id));
            category.setName(categoryRequest.getName());
            category.setStatus(categoryRequest.getStatus());
            Category savedCategory =  categoryRepository.save(category);
            log.info("CategoryService::updateProfile - Execution completed. [CategoryId: {}]", id);
            return categoryMapper.toDto(savedCategory);
        } catch (RuntimeException e) {
            log.error("CategoryService::updateProfile - Execution failed.", e);
            throw new BusinessException("CategoryService::updateProfile - Execution failed.");
        }
    }
    @Transactional
    public void deleteCategory(Long id) {
        log.info("CategoryService::deleteCategory - Execution started.");
        try {
            if (! categoryRepository.existsById(id)) {
                throw new EntityNotFoundException("Category not found with CategoryId: " + id);
            }
            categoryRepository.deleteById(id);
            log.info("CategoryService::deleteCategory - Execution completed.");
        } catch (Exception e) {
            log.error("CategoryService::deleteCategory - Execution failed.", e);
            throw new BusinessException("CategoryService::deleteCategory - Execution failed.");
        }
    }
}

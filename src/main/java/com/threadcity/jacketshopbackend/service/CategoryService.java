package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.filter.CategoryFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.CategoryRequest;
import com.threadcity.jacketshopbackend.dto.response.CategoryResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Category;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.CategoryMapper;
import com.threadcity.jacketshopbackend.repository.CategoryRepository;
import com.threadcity.jacketshopbackend.specification.CategorySpecification;
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
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryResponse getCategoryById(Long id) {
        log.info("CategoryService::getCategoryById - Execution started. [Id: {}]", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.CATEGORY_NOT_FOUND,
                        "Category not found with id: " + id));

        log.info("CategoryService::getCategoryById - Execution completed. [CategoryId: {}]", id);
        return categoryMapper.toDto(category);
    }

    public PageResponse<?> getAllCategories(CategoryFilterRequest request) {
        log.info("CategoryService::getAllCategories - Execution started.");

        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDir()),
                request.getSortBy());
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
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("CategoryService::createCategory - Execution started.");

        if (categoryRepository.existsByName(request.getName())) {
            throw new ResourceConflictException(ErrorCodes.CATEGORY_NAME_DUPLICATE,
                    "Category already exists with name: " + request.getName());
        }

        Category category = categoryMapper.toEntity(request);
        Category savedCategory = categoryRepository.save(category);

        log.info("CategoryService::createCategory - Execution completed.");
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public CategoryResponse updateCategoryById(CategoryRequest request, Long id) {
        log.info("CategoryService::updateCategoryById - Execution started.");

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.CATEGORY_NOT_FOUND,
                        "Category not found with id: " + id));

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setStatus(request.getStatus());

        Category savedCategory = categoryRepository.save(category);

        log.info("CategoryService::updateCategoryById - Execution completed. [CategoryId: {}]", id);
        return categoryMapper.toDto(savedCategory);
    }

    @Transactional
    public void deleteCategory(Long id) {
        log.info("CategoryService::deleteCategory - Execution started. [CategoryId: {}]", id);

        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.CATEGORY_NOT_FOUND, "Category not found with id: " + id);
        }

        categoryRepository.deleteById(id);
        log.info("CategoryService::deleteCategory - Execution completed. [CategoryId: {}]", id);
    }
}

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ProductRequest;
import com.threadcity.jacketshopbackend.dto.response.ProductResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.ProductMapper;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
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
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductResponse getProductById(Long Id) {
        log.info("ProductService::getProductById - Execution started. [Id: {}]", Id);
        Product product = productRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ProductId: " + Id));
        log.info("ProductService::getProductById - Execution completed. [ProductId: {}]", Id);
        return productMapper.toDto(product);
    }

    public PageResponse<?> getAllProduct(int page, int size, String sortBy) {
        log.info("ProductService::getAllProduct - Execution started.");
        try {
            int p = Math.max(0, page);
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Product> productPage = productRepository.findAll(pageable);
            List<ProductResponse> ProductList = productPage.stream()
                    .map(productMapper::toDto)
                    .toList();
            log.info("ProductService::getAllProduct - Execution completed.");
            return PageResponse.builder()
                    .contents(ProductList)
                    .size(size)
                    .page(p)
                    .totalPages(productPage.getTotalPages())
                    .totalElements(productPage.getTotalElements()).build();
        } catch (Exception e) {
            log.error("ProductService::getAllProduct - Execution failed.", e);
            throw new BusinessException("ProductService::getAllProduct - Execution failed.");
        }
    }
    @Transactional
    public ProductResponse createProduct(ProductRequest product) {
        log.info("ProductService::createProduct - Execution started.");
        if (productRepository.existsByName(product.getName())) {
            throw new BusinessException("Product already exists with name: " + product.getName());
        }
        try {
            Product productEntity = productMapper.toEntity(product);
            Product savedProduct = productRepository.save(productEntity);
            log.info("ProductService::createProduct - Execution completed.");
            return productMapper.toDto(savedProduct);
        } catch (Exception e) {
            log.error("ProductService::createProduct - Execution failed.", e);
            throw new BusinessException("ProductService::createProduct - Execution failed.");
        }
    }
    @Transactional
    public ProductResponse updateProductById(ProductRequest productRequest, Long id) {
        log.info("ProductService::updateProductById - Execution started.");
        try {
            Product product = productRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Product not found with ProductId: " + id));
            product.setName(productRequest.getName());
            product.setCategory(productRequest.getCategory());
            product.setBrand(productRequest.getBrand());
            product.setMaterial(productRequest.getMaterial());
            product.setImagesJson(productRequest.getImagesJson());
            product.setStyle(productRequest.getStyle());
            product.setDescription(productRequest.getDescription());
            product.setStatus(productRequest.getStatus());
            Product savedProduct = productRepository.save(product);
            log.info("ProductService::updateProfile - Execution completed. [ProductId: {}]", id);
            return productMapper.toDto(savedProduct);
        } catch (RuntimeException e) {
            log.error("ProductService::updateProfile - Execution failed.", e);
            throw new BusinessException("ProductService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("ProductService::deleteProduct - Execution started.");
        try {
            if (!productRepository.existsById(id)) {
                throw new EntityNotFoundException("Product not found with ProductId: " + id);
            }
            productRepository.deleteById(id);
            log.info("ProductService::deleteProduct - Execution completed.");
        } catch (Exception e) {
            log.error("ProductService::deleteProduct - Execution failed.", e);
            throw new BusinessException("ProductService::deleteProduct - Execution failed.");
        }
    }
}

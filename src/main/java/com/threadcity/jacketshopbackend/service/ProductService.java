package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.ProductFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ProductRequest;
import com.threadcity.jacketshopbackend.dto.response.ProductResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.ProductMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.specification.ProductSpecification;
import com.threadcity.jacketshopbackend.specification.UserSpecification;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final MaterialRepository materialRepository;
    private final StyleRepository styleRepository;
    private final ProductMapper productMapper;

    public ProductResponse getProductById(Long id) {
        log.info("ProductService::getProductById - Execution started. [id: {}]", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        log.info("ProductService::getProductById - Execution completed. [id: {}]", id);
        return productMapper.toDto(product);
    }

    public PageResponse<?> getAllProduct(ProductFilterRequest request) {
        log.info("ProductService::getAllProduct - Execution started.");
        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(),sort);

            Specification<Product> spec = ProductSpecification.buildSpec(request);
            Page<Product> productPage = productRepository.findAll(spec, pageable);

            List<ProductResponse> productResponse = productPage.getContent().stream()
                    .map(productMapper::toDto)
                    .toList();
            log.info("ProductService::getAllProduct - Execution completed.");
            return PageResponse.builder()
                    .contents(productResponse)
                    .size(request.getSize())
                    .page(request.getPage())
                    .totalPages(productPage.getTotalPages())
                    .totalElements(productPage.getTotalElements())
                    .build();

        } catch (Exception e) {
            log.error("ProductService::getAllProduct - Execution failed.", e);
            throw new BusinessException("Failed to get products.");
        }
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        log.info("ProductService::createProduct - Execution started.");

        if (productRepository.existsByName(req.getName())) {
            throw new BusinessException("Product already exists with name: " + req.getName());
        }

        try {
            Product product = new Product();
            product.setName(req.getName());
            product.setDescription(req.getDescription());
            product.setStatus(req.getStatus());
            product.setImagesJson(req.getImagesJson());

            if (req.getCategory() != null) {
                Category category = categoryRepository.findById(req.getCategory())
                        .orElseThrow(() -> new BusinessException("Category not found"));
                product.setCategory(category);
            }

            if (req.getBrand() != null) {
                Brand brand = brandRepository.findById(req.getBrand())
                        .orElseThrow(() -> new BusinessException("Brand not found"));
                product.setBrand(brand);
            }

            if (req.getMaterial() != null) {
                Material material = materialRepository.findById(req.getMaterial())
                        .orElseThrow(() -> new BusinessException("Material not found"));
                product.setMaterial(material);
            }

            if (req.getStyle() != null) {
                Style style = styleRepository.findById(req.getStyle())
                        .orElseThrow(() -> new BusinessException("Style not found"));
                product.setStyle(style);
            }

            Product saved = productRepository.save(product);
            log.info("ProductService::createProduct - Execution completed.");
            return productMapper.toDto(saved);

        } catch (Exception e) {
            log.error("ProductService::createProduct - Execution failed.", e);
            throw new BusinessException("Failed to create product.");
        }
    }

    @Transactional
    public ProductResponse updateProductById(ProductRequest req, Long id) {
        log.info("ProductService::updateProductById - Execution started. [id: {}]", id);

        try {
            Product product = productRepository.findById(id)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));

            // Lấy entity theo request (không phải theo id)
            if (req.getCategory() != null) {
                Category category = categoryRepository.findById(req.getCategory())
                        .orElseThrow(() -> new EntityNotFoundException("Category not found"));
                product.setCategory(category);
            }

            if (req.getBrand() != null) {
                Brand brand = brandRepository.findById(req.getBrand())
                        .orElseThrow(() -> new EntityNotFoundException("Brand not found"));
                product.setBrand(brand);
            }

            if (req.getMaterial() != null) {
                Material material = materialRepository.findById(req.getMaterial())
                        .orElseThrow(() -> new EntityNotFoundException("Material not found"));
                product.setMaterial(material);
            }

            if (req.getStyle() != null) {
                Style style = styleRepository.findById(req.getStyle())
                        .orElseThrow(() -> new EntityNotFoundException("Style not found"));
                product.setStyle(style);
            }

            product.setName(req.getName());
            product.setImagesJson(req.getImagesJson());
            product.setDescription(req.getDescription());
            product.setStatus(req.getStatus());

            Product saved = productRepository.save(product);

            log.info("ProductService::updateProductById - Execution completed. [id: {}]", id);
            return productMapper.toDto(saved);

        } catch (RuntimeException e) {
            log.error("ProductService::updateProductById - Execution failed.", e);
            throw new BusinessException("Failed to update product.");
        }
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("ProductService::deleteProduct - Execution started. [id: {}]", id);

        try {
            if (!productRepository.existsById(id)) {
                throw new EntityNotFoundException("Product not found with id: " + id);
            }
            productRepository.deleteById(id);

            log.info("ProductService::deleteProduct - Execution completed. [id: {}]", id);
        } catch (Exception e) {
            log.error("ProductService::deleteProduct - Execution failed.", e);
            throw new BusinessException("Failed to delete product.");
        }
    }

    // =============================
    // UPDATE STATUS (NEW)
    // =============================
    @Transactional
    public ProductResponse updateStatus(Long id, String status) {
        log.info("ProductService::updateStatus - Execution started. [id: {}]", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
        product.setStatus(Enums.Status.valueOf(status.toUpperCase()));
        Product saved = productRepository.save(product);

        log.info("ProductService::updateStatus - Execution completed. [id: {}]", id);
        return productMapper.toDto(saved);
    }

    // =============================
    // BULK UPDATE STATUS (NEW)
    // =============================
    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        log.info("ProductService::bulkUpdateStatus - Execution started.");

        List<Product> products = productRepository.findAllById(ids);
        products.forEach(p -> p.setStatus(Enums.Status.valueOf(status.toUpperCase())));
        productRepository.saveAll(products);

        log.info("ProductService::bulkUpdateStatus - Execution completed.");
    }

    // =============================
    // BULK DELETE (NEW)
    // =============================
    @Transactional
    public void bulkDelete(List<Long> ids) {
        log.info("ProductService::bulkDelete - Execution started.");

        List<Product> products = productRepository.findAllById(ids);

        if (products.size() != ids.size()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Một hoặc nhiều sản phẩm không tồn tại."
            );
        }

        productRepository.deleteAllInBatch(products);

        log.info("ProductService::bulkDelete - Execution completed.");
    }
}

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.ProductFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.ProductRequest;
import com.threadcity.jacketshopbackend.dto.response.ProductResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ProductMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.specification.ProductSpecification;
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + id));
        log.info("ProductService::getProductById - Execution completed. [id: {}]", id);
        return productMapper.toDto(product);
    }

    public PageResponse<?> getAllProduct(ProductFilterRequest request) {
        log.info("ProductService::getAllProduct - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

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
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        log.info("ProductService::createProduct - Execution started.");
        if (productRepository.existsByName(req.getName())) {
            throw new ResourceConflictException(ErrorCodes.PRODUCT_NAME_DUPLICATE,
                    "Product already exists with name: " + req.getName());
        }

        Product product = new Product();

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.CATEGORY_NOT_FOUND,
                            "Category not found with id: " + req.getCategoryId()));
            product.setCategory(category);
        }

        if (req.getBrandId() != null) {
            Brand brand = brandRepository.findById(req.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                            "Brand not found with id: " + req.getBrandId()));
            product.setBrand(brand);
        }

        if (req.getMaterialId() != null) {
            Material material = materialRepository.findById(req.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND,
                            "Material not found with id: " + req.getMaterialId()));
            product.setMaterial(material);
        }

        if (req.getStyleId() != null) {
            Style style = styleRepository.findById(req.getStyleId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND,
                            "Style not found with id: " + req.getStyleId()));
            product.setStyle(style);
        }

        product.setName(req.getName());
        product.setDescription(req.getDescription());
        product.setStatus(req.getStatus());
        product.setThumbnail(req.getThumbnail());
        Product saved = productRepository.save(product);

        log.info("ProductService::createProduct - Execution completed.");
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductResponse updateProductById(ProductRequest req, Long id) {
        log.info("ProductService::updateProductById - Execution started. [id: {}]", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + id));

        if (req.getCategoryId() != null) {
            Category category = categoryRepository.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.CATEGORY_NOT_FOUND,
                            "Category not found with id: " + req.getCategoryId()));
            product.setCategory(category);
        }

        if (req.getBrandId() != null) {
            Brand brand = brandRepository.findById(req.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                            "Brand not found with id: " + req.getBrandId()));
            System.out.println("bvbskvbdskj" + req.getBrandId());
            product.setBrand(brand);
        }

        if (req.getMaterialId() != null) {
            Material material = materialRepository.findById(req.getMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.MATERIAL_NOT_FOUND,
                            "Material not found with id: " + req.getMaterialId()));
            product.setMaterial(material);
        }

        if (req.getStyleId() != null) {
            Style style = styleRepository.findById(req.getStyleId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.STYLE_NOT_FOUND,
                            "Style not found with id: " + req.getStyleId()));
            product.setStyle(style);
        }

        product.setName(req.getName());
        product.setThumbnail(req.getThumbnail());
        product.setDescription(req.getDescription());
        product.setStatus(req.getStatus());
        Product saved = productRepository.save(product);

        log.info("ProductService::updateProductById - Execution completed. [id: {}]", id);
        return productMapper.toDto(saved);
    }

    @Transactional
    public void deleteProduct(Long id) {
        log.info("ProductService::deleteProduct - Execution started. [id: {}]", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found with id: " + id);
        }
        productRepository.deleteById(id);

        log.info("ProductService::deleteProduct - Execution completed. [id: {}]", id);
    }

    // =============================
    // UPDATE STATUS (NEW)
    // =============================
    @Transactional
    public ProductResponse updateStatus(Long id, String status) {
        log.info("ProductService::updateStatus - Execution started. [id: {}]", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + id));
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
            // Find missing IDs for better error message
            List<Long> foundIds = products.stream().map(Product::getId).toList();
            List<Long> missingIds = ids.stream().filter(id -> !foundIds.contains(id)).toList();
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Products not found: " + missingIds);
        }

        productRepository.deleteAllInBatch(products);

        log.info("ProductService::bulkDelete - Execution completed.");
    }
}

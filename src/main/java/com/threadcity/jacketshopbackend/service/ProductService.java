package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.product.request.ProductCreateRequest;
import com.threadcity.jacketshopbackend.dto.product.request.ProductUpdateRequest;
import com.threadcity.jacketshopbackend.dto.common.request.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.common.request.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.common.request.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.product.response.ProductResponse;
import com.threadcity.jacketshopbackend.entity.Brand;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.Style;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.ProductFilterRequest;
import com.threadcity.jacketshopbackend.mapper.ProductMapper;
import com.threadcity.jacketshopbackend.repository.BrandRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ProductVariantRepository;
import com.threadcity.jacketshopbackend.repository.StyleRepository;
import com.threadcity.jacketshopbackend.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;
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

    public PageResponse<?> getAllProducts(ProductFilterRequest request) {
        log.info("ProductService::getAllProducts - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Product> spec = ProductSpecification.buildSpec(request);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductResponse> productResponse = productPage.getContent().stream()
                .map(productMapper::toDto)
                .toList();
        log.info("ProductService::getAllProducts - Execution completed.");
        return PageResponse.builder()
                .contents(productResponse)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .build();
    }

    @Transactional
    public ProductResponse createProduct(ProductCreateRequest req) {
        log.info("ProductService::createProduct - Execution started.");
        if (productRepository.existsByName(req.getName())) {
            throw new ResourceConflictException(ErrorCodes.PRODUCT_NAME_DUPLICATE,
                    "Product already exists with name: " + req.getName());
        }

        Product product = new Product();

        if (req.getBrandId() != null) {
            Brand brand = brandRepository.findById(req.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                            "Brand not found with id: " + req.getBrandId()));
            product.setBrand(brand);
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

        if (req.getIsFeatured() != null)
            product.setIsFeatured(req.getIsFeatured());

        Product saved = productRepository.save(product);
        log.info("ProductService::createProduct - Execution completed.");
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductResponse updateProductById(ProductUpdateRequest req, Long id) {
        log.info("ProductService::updateProductById - Execution started. [id: {}]", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + id));

        if (req.getBrandId() != null) {
            Brand brand = brandRepository.findById(req.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.BRAND_NOT_FOUND,
                            "Brand not found with id: " + req.getBrandId()));
            product.setBrand(brand);
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

        if (req.getIsFeatured() != null)
            product.setIsFeatured(req.getIsFeatured());

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

    @Transactional
    public ProductResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("ProductService::updateStatus - Execution started. [id: {}]", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + id));
        product.setStatus(request.getStatus());
        Product saved = productRepository.save(product);

        log.info("ProductService::updateStatus - Execution completed. [id: {}]", id);
        return productMapper.toDto(saved);
    }

    @Transactional
    public List<ProductResponse> bulkUpdateProductsStatus(BulkStatusRequest request) {
        log.info("ProductService::bulkUpdateProductsStatus - Execution started.");

        List<Product> products = productRepository.findAllById(request.getIds());
        if (products.size() != request.getIds().size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Products not found: " + missingIds);
        }
        products.forEach(p -> p.setStatus(request.getStatus()));
        List<Product> savedProducts = productRepository.saveAll(products);

        log.info("ProductService::bulkUpdateProductsStatus - Execution completed.");
        return savedProducts.stream().map(productMapper::toDto).toList();
    }

    @Transactional
    public void bulkDeleteProducts(BulkDeleteRequest request) {
        log.info("ProductService::bulkDel teProducts - Execution started.");

        List<Product> products = productRepository.findAllById(request.getIds());

        if (products.size() != request.getIds().size()) {
            Set<Long> foundIds = products.stream().map(Product::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Products not found: " + missingIds);
        }

        productRepository.deleteAllInBatch(products);

        log.info("ProductService::bulkDeleteProducts - Execution completed.");
    }

    @Transactional
    public void syncProductData(Long productId) {
        log.info("ProductService::syncProductData - Execution started. [productId: {}]", productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + productId));

        List<ProductVariant> activeVariants = productVariantRepository.findAllByProductId(productId).stream()
                .filter(v -> v.getStatus() == Enums.Status.ACTIVE)
                .toList();

        if (activeVariants.isEmpty()) {
            product.setMinPrice(null);
            product.setMaxPrice(null);
        } else {
            BigDecimal minPrice = activeVariants.stream()
                    .map(ProductVariant::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            BigDecimal maxPrice = activeVariants.stream()
                    .map(ProductVariant::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);

            product.setMinPrice(minPrice);
            product.setMaxPrice(maxPrice);
        }

        productRepository.save(product);
        log.info("ProductService::syncProductData - Execution completed. [productId: {}]", productId);
    }
}

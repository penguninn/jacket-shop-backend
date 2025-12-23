package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ProductRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ProductResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ProductMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.filter.ProductFilterRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductVariantRepository productVariantRepository;
    @Mock private BrandRepository brandRepository;
    @Mock private StyleRepository styleRepository;
    @Mock private ProductMapper productMapper;

    @InjectMocks private ProductService productService;

    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ProductRequest mẫu
        productRequest = ProductRequest.builder()
                .name("Test Product")
                .description("Test Description")
                .status(Enums.Status.ACTIVE)
                .thumbnail("thumbnail.jpg")
                .isFeatured(true)
                .brandId(1L)
                .styleId(1L)
                .build();

        // Product mẫu
        product = Product.builder()
                .id(1L)
                .name("Test Product")
                .description("Test Description")
                .status(Enums.Status.ACTIVE)
                .thumbnail("thumbnail.jpg")
                .isFeatured(true)
                .colors(new HashSet<>())
                .materials(new HashSet<>())
                .sizes(new HashSet<>())
                .variants(new ArrayList<>())
                .build();
    }

    // =======================
    // Test getProductById
    // =======================
    @Test
    void testGetProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(ProductResponse.builder().id(1L).name("Test Product").build());

        ProductResponse response = productService.getProductById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Product", response.getName());
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(1L));
    }

    // =======================
    // Test createProduct
    // =======================
    @Test
    void testCreateProduct_Success() {
        when(productRepository.existsByName(productRequest.getName())).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(new Brand()));
        when(styleRepository.findById(1L)).thenReturn(Optional.of(new Style()));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(ProductResponse.builder().id(1L).name("Test Product").build());

        ProductResponse response = productService.createProduct(productRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testCreateProduct_DuplicateName() {
        when(productRepository.existsByName(productRequest.getName())).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> productService.createProduct(productRequest));
    }

    @Test
    void testCreateProduct_BrandNotFound() {
        when(productRepository.existsByName(productRequest.getName())).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(productRequest));
    }

    @Test
    void testCreateProduct_StyleNotFound() {
        when(productRepository.existsByName(productRequest.getName())).thenReturn(false);
        when(brandRepository.findById(1L)).thenReturn(Optional.of(new Brand()));
        when(styleRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.createProduct(productRequest));
    }

    // =======================
    // Test updateProductById
    // =======================
    @Test
    void testUpdateProductById_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(brandRepository.findById(1L)).thenReturn(Optional.of(new Brand()));
        when(styleRepository.findById(1L)).thenReturn(Optional.of(new Style()));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(ProductResponse.builder().id(1L).name("Test Product").build());

        ProductResponse response = productService.updateProductById(productRequest, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testUpdateProductById_NotFound() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> productService.updateProductById(productRequest, 1L));
    }

    // =======================
    // Test deleteProduct
    // =======================
    @Test
    void testDeleteProduct_Success() {
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

        assertDoesNotThrow(() -> productService.deleteProduct(1L));
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(1L));
    }

    // =======================
    // Test updateStatus
    // =======================
    @Test
    void testUpdateStatus_Success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(ProductResponse.builder().id(1L).name("Test Product").build());

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(Enums.Status.INACTIVE);

        ProductResponse response = productService.updateStatus(request, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(Enums.Status.INACTIVE, product.getStatus());
    }

    // =======================
    // Test bulkUpdateProductsStatus
    // =======================
    @Test
    void testBulkUpdateProductsStatus_Success() {
        List<Product> products = List.of(product);
        when(productRepository.findAllById(List.of(1L))).thenReturn(products);
        when(productRepository.saveAll(products)).thenReturn(products);
        when(productMapper.toDto(product)).thenReturn(ProductResponse.builder().id(1L).name("Test Product").build());

        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(List.of(1L));
        request.setStatus(Enums.Status.INACTIVE);

        List<ProductResponse> responses = productService.bulkUpdateProductsStatus(request);

        assertEquals(1, responses.size());
        assertEquals(Enums.Status.INACTIVE, product.getStatus());
    }

    @Test
    void testBulkUpdateProductsStatus_NotFound() {
        when(productRepository.findAllById(List.of(1L))).thenReturn(new ArrayList<>());

        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(List.of(1L));
        request.setStatus(Enums.Status.INACTIVE);

        assertThrows(ResourceNotFoundException.class, () -> productService.bulkUpdateProductsStatus(request));
    }

    // =======================
    // Test bulkDeleteProducts
    // =======================
    @Test
    void testBulkDeleteProducts_Success() {
        List<Product> products = List.of(product);
        when(productRepository.findAllById(List.of(1L))).thenReturn(products);
        doNothing().when(productRepository).deleteAllInBatch(products);

        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L));

        assertDoesNotThrow(() -> productService.bulkDeleteProducts(request));
    }

    @Test
    void testBulkDeleteProducts_NotFound() {
        when(productRepository.findAllById(List.of(1L))).thenReturn(new ArrayList<>());

        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L));

        assertThrows(ResourceNotFoundException.class, () -> productService.bulkDeleteProducts(request));
    }

    // =======================
    // Test syncProductData
    // =======================

    @Test
    void testSyncProductData_NoVariants() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productVariantRepository.findAllByProductId(1L)).thenReturn(new ArrayList<>());
        when(productRepository.save(product)).thenReturn(product);

        productService.syncProductData(1L);

        assertNull(product.getMinPrice());
        assertNull(product.getMaxPrice());
        assertTrue(product.getColors().isEmpty());
        assertTrue(product.getMaterials().isEmpty());
        assertTrue(product.getSizes().isEmpty());
    }
}

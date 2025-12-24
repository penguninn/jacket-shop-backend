package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.CouponValidateRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.CouponCreateRequest;
import com.threadcity.jacketshopbackend.dto.request.CouponUpdateRequest;
import com.threadcity.jacketshopbackend.dto.response.CouponResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Coupon;
import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.CouponMapper;
import com.threadcity.jacketshopbackend.repository.CouponRepository;
import com.threadcity.jacketshopbackend.filter.CouponFilterRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class CouponServiceTest {

    @Mock private CouponRepository couponRepository;
    @Mock private CouponMapper couponMapper;

    @InjectMocks private CouponService couponService;

    private Coupon coupon;
    private CouponCreateRequest createRequest;
    private CouponUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        createRequest = CouponCreateRequest.builder()
                .code("TESTCODE")
                .description("Test coupon")
                .type(Enums.CouponType.PERCENT)
                .value(new BigDecimal("10"))
                .minOrderValue(new BigDecimal("100"))
                .maxDiscount(new BigDecimal("50"))
                .usageLimit(10)
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(3600))
                .status(Enums.Status.ACTIVE)
                .build();

        updateRequest = CouponUpdateRequest.builder()
                .description("Updated coupon")
                .type(Enums.CouponType.PERCENT)
                .value(new BigDecimal("20"))
                .minOrderValue(new BigDecimal("150"))
                .maxDiscount(new BigDecimal("60"))
                .usageLimit(5)
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(7200))
                .status(Enums.Status.INACTIVE)
                .build();

        coupon = Coupon.builder()
                .id(1L)
                .code("TESTCODE")
                .description("Test coupon")
                .type(Enums.CouponType.PERCENT)
                .value(new BigDecimal("10"))
                .minOrderValue(new BigDecimal("100"))
                .maxDiscount(new BigDecimal("50"))
                .usageLimit(10)
                .usedCount(0)
                .validFrom(Instant.now())
                .validTo(Instant.now().plusSeconds(3600))
                .status(Enums.Status.ACTIVE)
                .build();
    }

    // =======================
    // Test getCouponById
    // =======================
    @Test
    void testGetCouponById_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponMapper.toDto(coupon)).thenReturn(CouponResponse.builder().id(1L).code("TESTCODE").build());

        CouponResponse response = couponService.getCouponById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
    }

    @Test
    void testGetCouponById_NotFound() {
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> couponService.getCouponById(1L));
    }

    // =======================
    // Test createCoupon
    // =======================
    @Test
    void testCreateCoupon_Success() {
        when(couponRepository.existsByCode("TESTCODE")).thenReturn(false);
        when(couponRepository.save(any(Coupon.class))).thenReturn(coupon);
        when(couponMapper.toDto(coupon)).thenReturn(CouponResponse.builder().id(1L).code("TESTCODE").build());

        CouponResponse response = couponService.createCoupon(createRequest);

        assertNotNull(response);
        assertEquals("TESTCODE", response.getCode());
    }

    @Test
    void testCreateCoupon_DuplicateCode() {
        when(couponRepository.existsByCode("TESTCODE")).thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> couponService.createCoupon(createRequest));
    }

    // =======================
    // Test updateCouponById
    // =======================
    @Test
    void testUpdateCouponById_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(couponMapper.toDto(coupon)).thenReturn(CouponResponse.builder().id(1L).code("TESTCODE").build());

        CouponResponse response = couponService.updateCouponById(1L, updateRequest);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Updated coupon", coupon.getDescription());
    }

    @Test
    void testUpdateCouponById_NotFound() {
        when(couponRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> couponService.updateCouponById(1L, updateRequest));
    }

    // =======================
    // Test deleteCoupon
    // =======================
    @Test
    void testDeleteCoupon_Success() {
        when(couponRepository.existsById(1L)).thenReturn(true);
        doNothing().when(couponRepository).deleteById(1L);

        assertDoesNotThrow(() -> couponService.deleteCoupon(1L));
    }

    @Test
    void testDeleteCoupon_NotFound() {
        when(couponRepository.existsById(1L)).thenReturn(false);
        assertThrows(ResourceNotFoundException.class, () -> couponService.deleteCoupon(1L));
    }

    // =======================
    // Test updateStatus
    // =======================
    @Test
    void testUpdateStatus_Success() {
        when(couponRepository.findById(1L)).thenReturn(Optional.of(coupon));
        when(couponRepository.save(coupon)).thenReturn(coupon);
        when(couponMapper.toDto(coupon)).thenReturn(CouponResponse.builder().id(1L).code("TESTCODE").build());

        UpdateStatusRequest request = new UpdateStatusRequest();
        request.setStatus(Enums.Status.INACTIVE);

        CouponResponse response = couponService.updateStatus(request, 1L);

        assertEquals(Enums.Status.INACTIVE, coupon.getStatus());
        assertNotNull(response);
    }

    // =======================
    // Test bulkUpdateCouponsStatus
    // =======================
    @Test
    void testBulkUpdateCouponsStatus_Success() {
        List<Coupon> coupons = List.of(coupon);
        when(couponRepository.findAllById(List.of(1L))).thenReturn(coupons);
        when(couponRepository.saveAll(coupons)).thenReturn(coupons);
        when(couponMapper.toDto(coupon)).thenReturn(CouponResponse.builder().id(1L).code("TESTCODE").build());

        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(List.of(1L));
        request.setStatus(Enums.Status.INACTIVE);

        List<CouponResponse> responses = couponService.bulkUpdateCouponsStatus(request);

        assertEquals(1, responses.size());
        assertEquals(Enums.Status.INACTIVE, coupon.getStatus());
    }

    @Test
    void testBulkUpdateCouponsStatus_NotFound() {
        when(couponRepository.findAllById(List.of(1L))).thenReturn(new ArrayList<>());

        BulkStatusRequest request = new BulkStatusRequest();
        request.setIds(List.of(1L));
        request.setStatus(Enums.Status.INACTIVE);

        assertThrows(ResourceNotFoundException.class, () -> couponService.bulkUpdateCouponsStatus(request));
    }

    // =======================
    // Test bulkDeleteCoupons
    // =======================
    @Test
    void testBulkDeleteCoupons_Success() {
        List<Coupon> coupons = List.of(coupon);
        when(couponRepository.findAllById(List.of(1L))).thenReturn(coupons);
        doNothing().when(couponRepository).deleteAllInBatch(coupons);

        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L));

        assertDoesNotThrow(() -> couponService.bulkDeleteCoupons(request));
    }

    @Test
    void testBulkDeleteCoupons_NotFound() {
        when(couponRepository.findAllById(List.of(1L))).thenReturn(new ArrayList<>());

        BulkDeleteRequest request = new BulkDeleteRequest();
        request.setIds(List.of(1L));

        assertThrows(ResourceNotFoundException.class, () -> couponService.bulkDeleteCoupons(request));
    }

    // =======================
    // Test validateCoupon
    // =======================
    @Test
    void testValidateCoupon_Success() {
        CouponValidateRequest request = CouponValidateRequest.builder()
                .code("TESTCODE")
                .orderAmount(new BigDecimal("200"))
                .build();

        when(couponRepository.findByCode("TESTCODE")).thenReturn(Optional.of(coupon));
        when(couponMapper.toDto(coupon)).thenReturn(CouponResponse.builder().id(1L).code("TESTCODE").build());

        CouponResponse response = couponService.validateCoupon(request);

        assertNotNull(response);
        assertEquals("TESTCODE", response.getCode());
    }

    @Test
    void testValidateCoupon_MinOrderValueNotReached() {
        CouponValidateRequest request = CouponValidateRequest.builder()
                .code("TESTCODE")
                .orderAmount(new BigDecimal("50"))
                .build();

        when(couponRepository.findByCode("TESTCODE")).thenReturn(Optional.of(coupon));

        assertThrows(InvalidRequestException.class, () -> couponService.validateCoupon(request));
    }

    @Test
    void testValidateCoupon_Expired() {
        coupon.setValidTo(Instant.now().minusSeconds(10));
        CouponValidateRequest request = CouponValidateRequest.builder()
                .code("TESTCODE")
                .orderAmount(new BigDecimal("200"))
                .build();

        when(couponRepository.findByCode("TESTCODE")).thenReturn(Optional.of(coupon));

        assertThrows(InvalidRequestException.class, () -> couponService.validateCoupon(request));
    }

}

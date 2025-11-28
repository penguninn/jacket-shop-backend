package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.CouponFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.CouponRequest;
import com.threadcity.jacketshopbackend.dto.request.SizeRequest;
import com.threadcity.jacketshopbackend.dto.response.CouponResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.SizeResponse;
import com.threadcity.jacketshopbackend.entity.Coupon;
import com.threadcity.jacketshopbackend.entity.Size;
import com.threadcity.jacketshopbackend.exception.BusinessException;
import com.threadcity.jacketshopbackend.mapper.CouponMapper;
import com.threadcity.jacketshopbackend.mapper.SizeMapper;
import com.threadcity.jacketshopbackend.repository.CouponRepository;
import com.threadcity.jacketshopbackend.repository.SizeRepository;
import com.threadcity.jacketshopbackend.specification.CouponSpecification;
import jakarta.persistence.EntityNotFoundException;
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
public class CouponService {
    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;
    public CouponResponse getCouponById(Long Id) {
        log.info("CouponService::getCouponById - Execution started. [Id: {}]", Id);
        Coupon coupon = couponRepository.findById(Id)
                .orElseThrow(() -> new EntityNotFoundException("Coupon not found with CouponId: " + Id));
        log.info("CouponService::getCouponById - Execution completed. [SizeId: {}]", Id);
        return couponMapper.toDto(coupon);
    }

    public PageResponse<?> getAllCoupons(CouponFilterRequest request) {
        log.info("CouponService::getAllCoupons - Execution started.");

        try {
            Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
            Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

            Specification<Coupon> spec = CouponSpecification.buildSpec(request);
            Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);

            List<CouponResponse> couponList = couponPage.getContent().stream()
                    .map(couponMapper::toDto)
                    .toList();

            log.info("CouponService::getAllCoupons - Execution completed.");
            return PageResponse.builder()
                    .contents(couponList)
                    .size(request.getSize())
                    .page(request.getPage())
                    .totalPages(couponPage.getTotalPages())
                    .totalElements(couponPage.getTotalElements())
                    .build();

        } catch (Exception e) {
            log.error("CouponService::getAllCoupons - Execution failed.", e);
            throw new BusinessException("CouponService::getAllCoupons - Execution failed.");
        }
    }

    @Transactional
    public CouponResponse createCoupon(CouponRequest coupon) {
        log.info("CouponService::createCoupon- Execution started.");
        if (couponRepository.existsByCode(coupon.getCode())) {
            throw new BusinessException("Coupon already exists with code: " + coupon.getCode());
        }
        try {
            Coupon couponEntity = couponMapper.toEntity(coupon);
            Coupon saveCoupon = couponRepository.save(couponEntity);
            log.info("CouponService::createCoupon - Execution completed.");
            return couponMapper.toDto(saveCoupon);
        } catch (Exception e) {
            log.error("CouponService::createCoupon - Execution failed.", e);
            throw new BusinessException("CouponService::createCoupon - Execution failed.");
        }
    }
    @Transactional
    public CouponResponse updateCouponById(CouponRequest couponRequest, Long id) {
        log.info("CouponService::updateCouponById - Execution started.");
        try {
            Coupon coupon = couponRepository.findById(id).orElseThrow(() ->
                    new EntityNotFoundException("Coupon not found with CouponId: " + id));
            coupon.setCode(couponRequest.getCode());
            coupon.setStatus(coupon.getStatus());
            Coupon saveCoupon = couponRepository.save(coupon);
            log.info("CouponService::updateProfile - Execution completed. [CouponId: {}]", id);
            return couponMapper.toDto(saveCoupon);
        } catch (RuntimeException e) {
            log.error("CouponService::updateProfile - Execution failed.", e);
            throw new BusinessException("CouponService::updateProfile - Execution failed.");
        }
    }

    @Transactional
    public void deleteCoupon(Long id) {
        log.info("CouponService::deleteCoupon - Execution started.");
        try {
            if (!couponRepository.existsById(id)) {
                throw new EntityNotFoundException("Coupon not found with CouponId: " + id);
            }
            couponRepository.deleteById(id);
            log.info("CouponService::deleteCoupon - Execution completed.");
        } catch (Exception e) {
            log.error("CouponService::deleteCoupon - Execution failed.", e);
            throw new BusinessException("CouponService::deleteCoupon - Execution failed.");
        }
    }
}

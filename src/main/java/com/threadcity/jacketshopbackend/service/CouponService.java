package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.request.CouponFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.CouponRequest;
import com.threadcity.jacketshopbackend.dto.response.CouponResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Coupon;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

import com.threadcity.jacketshopbackend.repository.CouponRepository;
import com.threadcity.jacketshopbackend.mapper.CouponMapper;
import com.threadcity.jacketshopbackend.specification.CouponSpecification;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponMapper couponMapper;

    public CouponResponse getCouponById(Long id) {
        log.info("CouponService::getCouponById - Execution started. [id: {}]", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND,
                        "Coupon not found with id: " + id));
        log.info("CouponService::getCouponById - Execution completed. [id: {}]", id);
        return couponMapper.toDto(coupon);
    }

    public PageResponse<?> getAllCoupons(CouponFilterRequest request) {
        log.info("CouponService::getAllCoupons - Execution started");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Coupon> spec = CouponSpecification.buildSpec(request);
        Page<Coupon> couponPage = couponRepository.findAll(spec, pageable);

        List<CouponResponse> responseList = couponPage.getContent().stream()
                .map(couponMapper::toDto)
                .toList();

        log.info("CouponService::getAllCoupons - Execution completed");
        return PageResponse.builder()
                .contents(responseList)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(couponPage.getTotalPages())
                .totalElements(couponPage.getTotalElements())
                .build();
    }

    @Transactional
    public CouponResponse createCoupon(CouponRequest req) {
        log.info("CouponService::createCoupon - Execution started");

        if (couponRepository.existsByCode(req.getCode())) {
            throw new ResourceConflictException(ErrorCodes.COUPON_CODE_DUPLICATE,
                    "Coupon already exists with code: " + req.getCode());
        }

        Coupon coupon = Coupon.builder()
                .usedCount(0)
                .code(req.getCode())
                .description(req.getDescription())
                .type(req.getType())
                .value(req.getValue())
                .minOrderValue(req.getMinOrderValue())
                .maxDiscount(req.getMaxDiscount())
                .usageLimit(req.getUsageLimit())
                .validFrom(req.getValidFrom())
                .validTo(req.getValidTo())
                .status(req.getStatus())
                .build();

        Coupon saved = couponRepository.save(coupon);
        log.info("CouponService::createCoupon - Execution completed");
        return couponMapper.toDto(saved);
    }

    @Transactional
    public CouponResponse updateCouponById(Long id, CouponRequest req) {
        log.info("CouponService::updateCouponById - Execution started. [id: {}]", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND,
                        "Coupon not found with id: " + id));

        coupon.setDescription(req.getDescription());
        coupon.setType(req.getType());
        coupon.setValue(req.getValue());
        coupon.setMinOrderValue(req.getMinOrderValue());
        coupon.setMaxDiscount(req.getMaxDiscount());
        coupon.setUsageLimit(req.getUsageLimit());
        coupon.setValidFrom(req.getValidFrom());
        coupon.setValidTo(req.getValidTo());
        coupon.setStatus(req.getStatus());

        Coupon saved = couponRepository.save(coupon);
        log.info("CouponService::updateCouponById - Execution completed. [id: {}]", id);
        return couponMapper.toDto(saved);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        log.info("CouponService::deleteCoupon - Execution started. [id: {}]", id);

        if (!couponRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupon not found with id: " + id);
        }
        couponRepository.deleteById(id);
        log.info("CouponService::deleteCoupon - Execution completed. [id: {}]", id);
    }

    @Transactional
    public CouponResponse updateStatus(Long id, String status) {
        log.info("CouponService::updateStatus - Execution started. [id: {}]", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND,
                        "Coupon not found with id: " + id));
        coupon.setStatus(Enums.Status.valueOf(status.toUpperCase()));
        Coupon saved = couponRepository.save(coupon);
        log.info("CouponService::updateStatus - Execution completed. [id: {}]", id);
        return couponMapper.toDto(saved);
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> ids, String status) {
        log.info("CouponService::bulkUpdateStatus - Execution started");
        List<Coupon> coupons = couponRepository.findAllById(ids);
        coupons.forEach(c -> c.setStatus(Enums.Status.valueOf(status.toUpperCase())));
        couponRepository.saveAll(coupons);
        log.info("CouponService::bulkUpdateStatus - Execution completed");
    }

    @Transactional
    public void bulkDelete(List<Long> ids) {
        log.info("CouponService::bulkDelete - Execution started");
        List<Coupon> coupons = couponRepository.findAllById(ids);

        if (coupons.size() != ids.size()) {
            throw new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "One or more coupons do not exist.");
        }

        couponRepository.deleteAllInBatch(coupons);
        log.info("CouponService::bulkDelete - Execution completed");
    }
}

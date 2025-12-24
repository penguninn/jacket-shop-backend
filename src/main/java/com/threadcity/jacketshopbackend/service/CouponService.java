package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;
import com.threadcity.jacketshopbackend.filter.CouponFilterRequest;
import com.threadcity.jacketshopbackend.dto.request.CouponCreateRequest;
import com.threadcity.jacketshopbackend.dto.request.CouponUpdateRequest;
import com.threadcity.jacketshopbackend.dto.response.CouponResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Coupon;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.CouponMapper;
import com.threadcity.jacketshopbackend.repository.CouponRepository;
import com.threadcity.jacketshopbackend.specification.CouponSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

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

    public CouponResponse getCouponByCode(String code) {
        log.info("CouponService::getCouponByCode - Execution started. [code: {}]", code);
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND,
                        "Coupon not found with code: " + code));

        validateCoupon(coupon);

        log.info("CouponService::getCouponByCode - Execution completed. [code: {}]", code);
        return couponMapper.toDto(coupon);
    }

    private void validateCoupon(Coupon coupon) {
        if (coupon.getStatus() != com.threadcity.jacketshopbackend.common.Enums.Status.ACTIVE) {
            throw new InvalidRequestException(ErrorCodes.COUPON_NOT_ACTIVE, "Coupon is not active");
        }
        Instant now = Instant.now();
        if (coupon.getValidFrom() != null && now.isBefore(coupon.getValidFrom())) {
            throw new InvalidRequestException(ErrorCodes.COUPON_NOT_STARTED, "Coupon is not yet valid");
        }
        if (coupon.getValidTo() != null && now.isAfter(coupon.getValidTo())) {
            throw new InvalidRequestException(ErrorCodes.COUPON_EXPIRED, "Coupon has expired");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageLimit() > 0
                && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new InvalidRequestException(ErrorCodes.COUPON_USAGE_LIMIT_REACHED, "Coupon usage limit reached");
        }
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
    public CouponResponse createCoupon(CouponCreateRequest req) {
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
    public CouponResponse updateCouponById(Long id, CouponUpdateRequest req) {
        log.info("CouponService::updateCouponById - Execution started. [id: {}]", id);

        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND,
                        "Coupon not found with id: " + id));

        // Code is immutable, do not update it.

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
    public CouponResponse updateStatus(UpdateStatusRequest request, Long id) {
        log.info("CouponService::updateStatus - Execution started. [id: {}]", id);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND,
                        "Coupon not found with id: " + id));
        coupon.setStatus(request.getStatus());
        Coupon saved = couponRepository.save(coupon);
        log.info("CouponService::updateStatus - Execution completed. [id: {}]", id);
        return couponMapper.toDto(saved);
    }

    @Transactional
    public List<CouponResponse> bulkUpdateCouponsStatus(BulkStatusRequest request) {
        log.info("CouponService::bulkUpdateCouponsStatus - Execution started");

        List<Coupon> coupons = couponRepository.findAllById(request.getIds());
        if (coupons.size() != request.getIds().size()) {
            Set<Long> foundIds = coupons.stream().map(Coupon::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupons not found: " + missingIds);
        }

        coupons.forEach(c -> c.setStatus(request.getStatus()));
        List<Coupon> savedCoupons = couponRepository.saveAll(coupons);
        log.info("CouponService::bulkUpdateCouponsStatus - Execution completed");
        return savedCoupons.stream().map(couponMapper::toDto).toList();
    }

    @Transactional
    public void bulkDeleteCoupons(BulkDeleteRequest request) {
        log.info("CouponService::bulkDeleteCoupons - Execution started");

        List<Coupon> coupons = couponRepository.findAllById(request.getIds());

        if (coupons.size() != request.getIds().size()) {
            Set<Long> foundIds = coupons.stream().map(Coupon::getId).collect(Collectors.toSet());
            Set<Long> missingIds = new HashSet<>(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupons not found: " + missingIds);
        }

        couponRepository.deleteAllInBatch(coupons);
        log.info("CouponService::bulkDeleteCoupons - Execution completed");
    }
}

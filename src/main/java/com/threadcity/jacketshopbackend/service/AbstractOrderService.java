package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.request.ShippingInfoRequest;
import com.threadcity.jacketshopbackend.dto.request.UpdatePaymentRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderHistoryResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import com.threadcity.jacketshopbackend.specification.OrderSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

@Slf4j
public abstract class AbstractOrderService {

    protected final OrderRepository orderRepository;
    protected final ProductVariantRepository productVariantRepository;
    protected final PaymentMethodRepository paymentMethodRepository;
    protected final CouponRepository couponRepository;
    protected final UserRepository userRepository;
    protected final AddressRepository addressRepository;
    protected final OrderHistoryRepository orderHistoryRepository;
    protected final CartService cartService;
    protected final ProductVariantService productVariantService;
    protected final OrderMapper orderMapper;

    protected AbstractOrderService(OrderRepository orderRepository,
                                   ProductVariantRepository productVariantRepository,
                                   PaymentMethodRepository paymentMethodRepository,
                                   CouponRepository couponRepository,
                                   UserRepository userRepository,
                                   AddressRepository addressRepository,
                                   OrderHistoryRepository orderHistoryRepository,
                                   CartService cartService,
                                   ProductVariantService productVariantService,
                                   OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.productVariantRepository = productVariantRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.couponRepository = couponRepository;
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.orderHistoryRepository = orderHistoryRepository;
        this.cartService = cartService;
        this.productVariantService = productVariantService;
        this.orderMapper = orderMapper;
    }

    // Abstract method for stock handling during creation
    protected abstract void handleStockForCreate(Long variantId, Integer quantity);

    public List<OrderHistoryResponse> getOrderHistory(Long orderId) {
        log.info("AbstractOrderService::getOrderHistory - Execution started. [orderId: {}]", orderId);

        if (!orderRepository.existsById(orderId)) {
            throw new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found");
        }

        List<OrderHistory> histories = orderHistoryRepository.findByOrderIdOrderByCreatedAtDesc(orderId);

        List<OrderHistoryResponse> response = histories.stream().map(h -> OrderHistoryResponse.builder()
                .id(h.getId())
                .orderId(h.getOrder().getId())
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .oldPaymentStatus(h.getOldPaymentStatus())
                .newPaymentStatus(h.getNewPaymentStatus())
                .changedByUserId(h.getChangedByUserId())
                .note(h.getNote())
                .createdAt(h.getCreatedAt())
                .build()).toList();

        log.info("AbstractOrderService::getOrderHistory - Execution completed. [orderId: {}]", orderId);
        return response;
    }

    public PageResponse<?> getAllOrders(OrderFilterRequest request) {
        log.info("AbstractOrderService::getAllOrders - Execution started.");
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Specification<Order> spec = OrderSpecification.buildSpec(request);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(orderMapper::toDto)
                .toList();
        log.info("AbstractOrderService::getAllOrders - Execution completed.");

        return PageResponse.builder()
                .contents(responses)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    public List<OrderResponse> getMyOrders(OrderStatus status) {
        log.info("AbstractOrderService::getMyOrders - Execution started. [status: {}]", status);
        Long userId = getUserId();

        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setUserId(userId);
        filterRequest.setStatus(status);
        filterRequest.setSortBy("createdAt");
        filterRequest.setSortDir("desc");
        filterRequest.setSize(100);

        Specification<Order> spec = OrderSpecification.buildSpec(filterRequest);
        List<Order> orders = orderRepository.findAll(spec);

        log.info("AbstractOrderService::getMyOrders - Execution completed.");
        return orders.stream().map(orderMapper::toDto).toList();
    }

    public List<OrderResponse> getMyOrders() {
        return getMyOrders(null);
    }

    public OrderResponse getOrderById(Long id) {
        log.info("AbstractOrderService::getOrderById - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        log.info("AbstractOrderService::getOrderById - Execution completed.");
        return orderMapper.toDto(order);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long id, UpdatePaymentRequest request) {
        log.info("AbstractOrderService::updatePaymentStatus - Execution started. [id: {}, status: {}]", id, request.getPaymentStatus());
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        PaymentStatus oldPaymentStatus = order.getPaymentStatus();
        
        if (request.getPaymentMethodId() != null) {
            PaymentMethod pm = paymentMethodRepository.findById(request.getPaymentMethodId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Payment method not found"));
            if (pm.getStatus() != Status.ACTIVE) {
                 throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Payment method is not active");
            }
            order.setPaymentMethod(pm);
            order.setPaymentMethodName(pm.getName());
        }

        order.setPaymentStatus(request.getPaymentStatus());
        if (request.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentDate(Instant.now());
        }
        Order saved = orderRepository.save(order);

        saveOrderHistory(saved, order.getStatus(), oldPaymentStatus, "Payment status updated");

        log.info("AbstractOrderService::updatePaymentStatus - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse updateShippingInfo(Long id, ShippingInfoRequest request) {
        log.info("AbstractOrderService::updateShippingInfo - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Cannot update shipping info after order is shipping");
        }

        order.setCarrierName(request.getCarrierName());
        order.setCarrierServiceName(request.getCarrierServiceName());

        if (request.getShippingFee() != null) {
            order.setShippingFee(request.getShippingFee());
            BigDecimal total = order.getSubtotal().add(order.getShippingFee()).subtract(order.getDiscount());
            if (total.compareTo(BigDecimal.ZERO) < 0) total = BigDecimal.ZERO;
            order.setTotal(total);
        }

        Order saved = orderRepository.save(order);

        saveOrderHistory(saved, saved.getStatus(), saved.getPaymentStatus(), "Shipping info updated");

        log.info("AbstractOrderService::updateShippingInfo - Execution completed.");
        return orderMapper.toDto(saved);
    }
    
    @Transactional
    public OrderResponse requestReturn(Long id, String reason) {
        log.info("AbstractOrderService::requestReturn - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        Long currentUserId = getUserId();
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be COMPLETED to request return");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.RETURNED);

        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Return requested: " + reason);
        
        log.info("AbstractOrderService::requestReturn - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse approveReturn(Long id) {
        log.info("AbstractOrderService::approveReturn - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.RETURNED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be in RETURNED status to approve return");
        }

        PaymentStatus oldPaymentStatus = order.getPaymentStatus();
        productVariantService.returnStock(order.getDetails());

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, order.getStatus(), oldPaymentStatus, "Return approved by admin");

        log.info("AbstractOrderService::approveReturn - Execution completed.");
        return orderMapper.toDto(saved);
    }

    // --- Protected Helpers ---

    protected void incrementCouponUsage(String couponCode) {
        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupon not found"));
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }
    }

    protected void saveOrderHistory(Order order, OrderStatus oldStatus, PaymentStatus oldPaymentStatus, String note) {
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(order.getStatus())
                .oldPaymentStatus(oldPaymentStatus)
                .newPaymentStatus(order.getPaymentStatus())
                .changedByUserId(getUserIdSafe())
                .note(note)
                .build();
        orderHistoryRepository.save(history);
    }

    protected Long getUserIdSafe() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
                return ((UserDetailsImpl) authentication.getPrincipal()).getId();
            }
        } catch (Exception e) {
            log.warn("Could not get user ID for order history: {}", e.getMessage());
        }
        return null;
    }

    protected Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

    protected String generateOrderCode() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + randomPart;
    }

    protected void handleShippingInfo(Order order, OrderRequest request) {
        // Shared logic: if instore, 0 fee; else if addressId provided, fill address; else manual
        if (request.getOrderType() == com.threadcity.jacketshopbackend.common.Enums.OrderType.POS_INSTORE) {
            order.setShippingFee(BigDecimal.ZERO);
            return;
        }

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Address not found"));

            order.setShippingRecipientName(address.getRecipientName());
            order.setShippingRecipientPhone(address.getRecipientPhone());
            order.setShippingAddressLine(address.getAddressLine());
            order.setShippingProvinceCode(address.getProvince().getGoshipId());
            order.setShippingDistrictCode(address.getDistrict().getGoshipId());
            order.setShippingWardCode(address.getWard().getGoshipId());
            order.setShippingProvinceName(address.getProvince().getName());
            order.setShippingDistrictName(address.getDistrict().getName());
            order.setShippingWardName(address.getWard().getName());
        }

        order.setCarrierName(request.getCarrierName());
        order.setCarrierServiceName(request.getCarrierServiceName());
        order.setCarrierRateId(request.getCarrierRateId());
        order.setDeliveryTimeEstimate(request.getDeliveryTimeEstimate());
        order.setShippingFee(request.getShippingFee() != null ? request.getShippingFee() : BigDecimal.ZERO);
    }

    protected void processOrderItems(Order order, List<OrderItemRequest> items) {
        processOrderItems(order, items, true);
    }

    protected void processOrderItems(Order order, List<OrderItemRequest> items, boolean applyStock) {
        List<OrderDetail> details = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : items) {
            ProductVariant variant = productVariantRepository.findById(itemReq.getProductVariantId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                                    "Variant not found"));

            if (variant.getStatus() != Status.ACTIVE) {
                throw new InvalidRequestException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "Product " + variant.getProduct().getName() + " is currently unavailable");
            }

            // Hook for stock (Reserve or Deduct)
            if (applyStock) {
                handleStockForCreate(variant.getId(), itemReq.getQuantity());
            }

            // Calculate Effective Price (Applying variant sales)
            BigDecimal originalPrice = variant.getPrice();
            BigDecimal effectivePrice = originalPrice;
            BigDecimal discountPercentage = BigDecimal.ZERO;

            Sale bestSale = getBestSale(variant);
            if (bestSale != null) {
                discountPercentage = bestSale.getDiscountPercentage();
                BigDecimal discountFactor = discountPercentage.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
                BigDecimal discountAmount = originalPrice.multiply(discountFactor);
                effectivePrice = originalPrice.subtract(discountAmount);
            }

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .sku(variant.getSku())
                    .size(variant.getSize().getName())
                    .color(variant.getColor().getName())
                    .material(variant.getMaterial().getName())
                    .image(variant.getImage())
                    .price(effectivePrice) // Price paid
                    .originalPrice(originalPrice)
                    .discountPercentage(discountPercentage)
                    .quantity(itemReq.getQuantity())
                    .build();
            details.add(detail);

            BigDecimal lineTotal = detail.getSubtotal();
            subtotal = subtotal.add(lineTotal);
        }

        order.setDetails(details);
        order.setSubtotal(subtotal);
    }

    protected Sale getBestSale(ProductVariant variant) {
        List<Sale> sales = variant.getSales();
        if (sales == null || sales.isEmpty()) return null;

        LocalDateTime now = LocalDateTime.now();
        return sales.stream()
            .filter(sale -> {
                if (sale.getDiscountPercentage() == null) return false;
                boolean startOk = sale.getStartDate() == null || !now.isBefore(sale.getStartDate());
                boolean endOk = sale.getEndDate() == null || !now.isAfter(sale.getEndDate());
                return startOk && endOk;
            })
            .max(Comparator.comparing(Sale::getDiscountPercentage))
            .orElse(null);
    }

    protected void calculateFinancials(Order order, OrderRequest request) {
        BigDecimal discount = BigDecimal.ZERO;
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupon not found"));

            validateCoupon(coupon, order.getSubtotal());
            order.setCouponCode(coupon.getCode());
            discount = calculateDiscount(coupon, order.getSubtotal());
        }
        order.setDiscount(discount);

        BigDecimal total = order.getSubtotal()
                .add(order.getShippingFee())
                .subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0)
            total = BigDecimal.ZERO;

        order.setTotal(total);
    }

    protected void validateCoupon(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getStatus() != Status.ACTIVE) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Coupon is not active");
        }
        Instant now = Instant.now();
        if (now.isBefore(coupon.getValidFrom()) || now.isAfter(coupon.getValidTo())) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Coupon is expired or not yet valid");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Coupon usage limit reached");
        }
        if (coupon.getMinOrderValue() != null && subtotal.compareTo(coupon.getMinOrderValue()) < 0) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                    "Minimum order value not met for this coupon");
        }
    }

    protected BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getType() == com.threadcity.jacketshopbackend.common.Enums.CouponType.AMOUNT) {
            discount = coupon.getValue();
        } else if (coupon.getType() == com.threadcity.jacketshopbackend.common.Enums.CouponType.PERCENT) {
            discount = subtotal.multiply(coupon.getValue().divide(new BigDecimal(100)));
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        }
        return discount.min(subtotal);
    }

    protected void configurePaymentAndStatus(Order order, OrderRequest request) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Payment method not found"));

        if (paymentMethod.getStatus() != Status.ACTIVE) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Payment method is not active");
        }

        order.setPaymentMethod(paymentMethod);
        order.setPaymentMethodName(paymentMethod.getName());
    }
}

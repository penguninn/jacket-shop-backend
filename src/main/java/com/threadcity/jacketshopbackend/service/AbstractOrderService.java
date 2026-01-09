package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.order.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.order.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.order.request.ShippingInfoRequest;
import com.threadcity.jacketshopbackend.dto.order.request.UpdatePaymentRequest;
import com.threadcity.jacketshopbackend.dto.order.response.OrderHistoryResponse;
import com.threadcity.jacketshopbackend.dto.order.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.specification.OrderSpecification;
import com.threadcity.jacketshopbackend.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractOrderService {

    protected final OrderRepository orderRepository;
    protected final ProductVariantRepository productVariantRepository;
    protected final PaymentMethodRepository paymentMethodRepository;
    protected final UserRepository userRepository;
    protected final AddressRepository addressRepository;
    protected final OrderHistoryRepository orderHistoryRepository;

    protected final StockService stockService;
    protected final PricingService pricingService;
    protected final CouponService couponService;

    protected final CartService cartService;

    protected final OrderMapper orderMapper;

    /**
     * Get order by ID with ownership check.
     */
    public OrderResponse getOrderById(Long id) {
        log.info("AbstractOrderService::getOrderById - Start [id: {}]", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        SecurityUtils.requireOwnership(order.getUser().getId(), "order");

        log.info("AbstractOrderService::getOrderById - Completed");
        return orderMapper.toDto(order);
    }

    /**
     * Get all orders with filtering and pagination.
     */
    public PageResponse<?> getAllOrders(OrderFilterRequest request) {
        log.info("AbstractOrderService::getAllOrders - Start");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Specification<Order> spec = OrderSpecification.buildSpec(request);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(orderMapper::toDto)
                .toList();

        log.info("AbstractOrderService::getAllOrders - Completed [count: {}]", responses.size());
        return PageResponse.builder()
                .contents(responses)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    /**
     * Get current user's orders filtered by status.
     */
    public List<OrderResponse> getMyOrders(OrderStatus status) {
        log.info("AbstractOrderService::getMyOrders - Start [status: {}]", status);

        Long userId = getUserId();

        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setUserId(userId);
        filterRequest.setStatus(status);
        filterRequest.setSortBy("createdAt");
        filterRequest.setSortDir("desc");
        filterRequest.setSize(100);

        Specification<Order> spec = OrderSpecification.buildSpec(filterRequest);
        List<Order> orders = orderRepository.findAll(spec);

        log.info("AbstractOrderService::getMyOrders - Completed [count: {}]", orders.size());
        return orders.stream().map(orderMapper::toDto).toList();
    }

    /**
     * Get order history for an order.
     */
    public List<OrderHistoryResponse> getOrderHistory(Long orderId) {
        log.info("AbstractOrderService::getOrderHistory - Start [orderId: {}]", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        SecurityUtils.requireOwnership(order.getUser().getId(), "order");

        List<OrderHistory> histories = orderHistoryRepository.findByOrderIdOrderByCreatedAtDesc(orderId);

        List<OrderHistoryResponse> response = histories.stream().map(h -> OrderHistoryResponse.builder()
                .id(h.getId())
                .orderId(h.getOrder().getId())
                .oldStatus(h.getOldStatus())
                .newStatus(h.getNewStatus())
                .oldPaymentStatus(h.getOldPaymentStatus())
                .newPaymentStatus(h.getNewPaymentStatus())
                .changedByUserId(h.getChangedByUser() != null ? h.getChangedByUser().getId() : null)
                .note(h.getNote())
                .createdAt(h.getCreatedAt())
                .build()).toList();

        log.info("AbstractOrderService::getOrderHistory - Completed");
        return response;
    }

    /**
     * Update payment status of an order.
     */
    @Transactional
    public OrderResponse updatePaymentStatus(Long id, UpdatePaymentRequest request) {
        log.info("AbstractOrderService::updatePaymentStatus - Start [id: {}, status: {}]", id, request.getPaymentStatus());

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

        log.info("AbstractOrderService::updatePaymentStatus - Completed");
        return orderMapper.toDto(saved);
    }

    /**
     * Update shipping info of an order.
     */
    @Transactional
    public OrderResponse updateShippingInfo(Long id, ShippingInfoRequest request) {
        log.info("AbstractOrderService::updateShippingInfo - Start [id: {}]", id);

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
            recalculateTotal(order);
        }

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, saved.getStatus(), saved.getPaymentStatus(), "Shipping info updated");

        log.info("AbstractOrderService::updateShippingInfo - Completed");
        return orderMapper.toDto(saved);
    }

    // ==================== RETURN OPERATIONS ====================

    /**
     * Customer requests return for a completed order.
     */
    @Transactional
    public OrderResponse requestReturn(Long id, String reason) {
        log.info("AbstractOrderService::requestReturn - Start [id: {}]", id);

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
        order.setReturnedAt(Instant.now());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Return requested: " + reason);

        log.info("AbstractOrderService::requestReturn - Completed");
        return orderMapper.toDto(saved);
    }

    /**
     * Admin approves return request.
     * Returns stock and refunds payment if applicable.
     */
    @Transactional
    public OrderResponse approveReturn(Long id) {
        log.info("AbstractOrderService::approveReturn - Start [id: {}]", id);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.RETURNED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be in RETURNED status to approve return");
        }

        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Return stock using StockService
        stockService.returnStock(new ArrayList<>(order.getOrderDetails()));

        // Decrement coupon usage
        couponService.decrementUsage(order.getCouponCode());

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, order.getStatus(), oldPaymentStatus, "Return approved");

        log.info("AbstractOrderService::approveReturn - Completed");
        return orderMapper.toDto(saved);
    }

    // ==================== PROTECTED HELPERS ====================
    protected String generateOrderCode() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + randomPart;
    }

    protected void handleShippingInfo(Order order, OrderRequest request) {
        if (request.getOrderType() == OrderType.POS_INSTORE) {
            order.setShippingFee(BigDecimal.ZERO);
            return;
        }

        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ADDRESS_NOT_FOUND, "Address not found"));

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

    protected void processOrderItems(Order order, List<OrderItemRequest> items, boolean reserveStock) {
        Set<OrderDetail> details = new LinkedHashSet<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : items) {
            ProductVariant variant = productVariantRepository.findById(itemReq.getProductVariantId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                            "Variant not found: " + itemReq.getProductVariantId()));

            if (variant.getStatus() != Status.ACTIVE) {
                throw new InvalidRequestException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "Product " + variant.getProduct().getName() + " is currently unavailable");
            }

            // Reserve stock using StockService
            if (reserveStock) {
                stockService.reserveStock(variant.getId(), itemReq.getQuantity());
            }

            // Calculate pricing using PricingService
            PricingService.PriceResult priceResult = pricingService.calculatePrice(variant);

            OrderDetail detail = OrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .sku(variant.getSku())
                    .size(variant.getSize().getName())
                    .color(variant.getColor().getName())
                    .material(variant.getMaterial().getName())
                    .image(variant.getImage())
                    .price(priceResult.getFinalPrice())
                    .originalPrice(priceResult.getOriginalPrice())
                    .discountPercentage(priceResult.getDiscountPercentage())
                    .quantity(itemReq.getQuantity())
                    .build();
            details.add(detail);

            BigDecimal lineTotal = priceResult.getFinalPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            subtotal = subtotal.add(lineTotal);
        }

        order.setOrderDetails(details);
        order.setSubtotal(subtotal);
    }

    /**
     * Calculate financials: coupon discount and total.
     * Uses CouponService for validation and calculation.
     */
    protected void calculateFinancials(Order order, String couponCode) {
        BigDecimal discount = BigDecimal.ZERO;

        if (couponCode != null && !couponCode.isBlank()) {
            Coupon coupon = couponService.findAndValidate(couponCode, order.getSubtotal());
            if (coupon != null) {
                order.setCouponCode(coupon.getCode());
                order.setCoupon(coupon);
                discount = couponService.calculateDiscount(coupon, order.getSubtotal());
            }
        }

        order.setDiscount(discount);
        recalculateTotal(order);
    }

    /**
     * Recalculate order total from subtotal, shipping, and discount.
     */
    protected void recalculateTotal(Order order) {
        BigDecimal shippingFee = order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO;
        BigDecimal discount = order.getDiscount() != null ? order.getDiscount() : BigDecimal.ZERO;
        BigDecimal total = order.getSubtotal().add(shippingFee).subtract(discount);

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }

        order.setTotal(total);
    }

    /**
     * Configure payment method and validate.
     */
    protected void configurePaymentMethod(Order order, Long paymentMethodId) {
        if (paymentMethodId == null) {
            return;
        }

        PaymentMethod paymentMethod = paymentMethodRepository.findById(paymentMethodId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Payment method not found"));

        if (paymentMethod.getStatus() != Status.ACTIVE) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Payment method is not active");
        }

        order.setPaymentMethod(paymentMethod);
        order.setPaymentMethodName(paymentMethod.getName());
    }

    /**
     * Save order history entry.
     */
    protected void saveOrderHistory(Order order, OrderStatus oldStatus, PaymentStatus oldPaymentStatus, String note) {
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(order.getStatus())
                .oldPaymentStatus(oldPaymentStatus)
                .newPaymentStatus(order.getPaymentStatus())
                .changedByUser(getCurrentUser())
                .note(note)
                .build();
        orderHistoryRepository.save(history);
    }

    /**
     * Get current user entity safely (returns null if not authenticated).
     */
    protected User getCurrentUser() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElse(null);
    }

    /**
     * Get current user ID (throws if not authenticated).
     */
    protected Long getUserId() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            throw new InvalidRequestException(ErrorCodes.AUTH_INVALID_CREDENTIALS, "User not authenticated");
        }
        return userId;
    }
}

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderHistoryResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.threadcity.jacketshopbackend.dto.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.specification.OrderSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderHistoryRepository orderHistoryRepository;

    private final CartService cartService;
    private final ProductVariantService productVariantService;
    private final OrderMapper orderMapper;

    private void saveOrderHistory(Order order, OrderStatus oldStatus, PaymentStatus oldPaymentStatus, String note) {
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

    private Long getUserIdSafe() {
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

    public List<OrderHistoryResponse> getOrderHistory(Long orderId) {
        log.info("OrderService::getOrderHistory - Execution started. [orderId: {}]", orderId);

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
        
        log.info("OrderService::getOrderHistory - Execution completed. [orderId: {}]", orderId);
        return response;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("OrderService::createOrder - Start [Type: {}]", request.getOrderType());

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(request.getOrderType());
        order.setNote(request.getNote());

        User user = null;
        if (request.getOrderType() == OrderType.ONLINE) {
            Long userId = getUserId();
            user = userRepository.getReferenceById(userId);
            order.setUser(user);
        } else {
            if (request.getUserId() != null) {
                user = userRepository.findById(request.getUserId())
                        .orElseThrow(
                                () -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "Member not found"));
                order.setUser(user);
            }
            if (request.getOrderType() == OrderType.POS_INSTORE) {
                Long staffId = getUserId();
                User staff = userRepository.getReferenceById(staffId);
                order.setStaff(staff);
            }
        }

        order.setCustomerName(user.getFullName());
        order.setCustomerPhone(user.getPhone());

        handleShippingInfo(order, request);

        processOrderItems(order, request.getItems());

        calculateFinancials(order, request);

        configurePaymentAndStatus(order, request);

        Order savedOrder = orderRepository.save(order);

        saveOrderHistory(savedOrder, null, null, "Order created");

        if (request.getOrderType() == OrderType.ONLINE) {
            cartService.clearCart();
        }

        log.info("OrderService::createOrder - Success [Code: {}]", savedOrder.getOrderCode());
        return orderMapper.toDto(savedOrder);
    }

    public PageResponse<?> getAllOrders(OrderFilterRequest request) {
        log.info("OrderService::getAllOrders - Execution started.");
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);
        Specification<Order> spec = OrderSpecification.buildSpec(request);
        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponse> responses = orderPage.getContent().stream()
                .map(orderMapper::toDto)
                .toList();
        log.info("OrderService::getAllOrders - Execution completed.");

        return PageResponse.builder()
                .contents(responses)
                .page(request.getPage())
                .size(request.getSize())
                .totalElements(orderPage.getTotalElements())
                .totalPages(orderPage.getTotalPages())
                .build();
    }

    public List<OrderResponse> getMyOrders(OrderStatus status) {
        log.info("OrderService::getMyOrders - Execution started. [status: {}]", status);
        Long userId = getUserId();

        OrderFilterRequest filterRequest = new OrderFilterRequest();
        filterRequest.setUserId(userId);
        filterRequest.setStatus(status);
        filterRequest.setSortBy("createdAt");
        filterRequest.setSortDir("desc");
        filterRequest.setSize(100);

        Specification<Order> spec = OrderSpecification.buildSpec(filterRequest);
        List<Order> orders = orderRepository.findAll(spec);

        log.info("OrderService::getMyOrders - Execution completed.");
        return orders.stream().map(orderMapper::toDto).toList();
    }

    public List<OrderResponse> getMyOrders() {
        return getMyOrders(null);
    }

    public OrderResponse getOrderById(Long id) {
        log.info("OrderService::getOrderById - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        log.info("OrderService::getOrderById - Execution completed.");
        return orderMapper.toDto(order);
    }

    @Transactional
    public void reorder(Long id) {
        log.info("OrderService::reorder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        Long currentUserId = getUserId();
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only reorder your own orders.");
        }

        for (OrderDetail detail : order.getDetails()) {
            try {
                CartItemRequest cartItemRequest = new CartItemRequest();
                cartItemRequest.setProductVariantId(detail.getProductVariant().getId());
                cartItemRequest.setQuantity(detail.getQuantity());
                cartService.addToCart(cartItemRequest);
            } catch (Exception e) {
                log.warn("Could not add item {} to cart during reorder: {}", detail.getSku(), e.getMessage());
            }
        }
        log.info("OrderService::reorder - Execution completed.");
    }

    @Transactional
    public OrderResponse receiveOrder(Long id) {
        log.info("OrderService::receiveOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        Long currentUserId = getUserId();
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        if (order.getStatus() != OrderStatus.SHIPPING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be SHIPPING or CONFIRMED to receive");
        }

        return completeOrder(id);
    }

    @Transactional
    public OrderResponse requestReturn(Long id, String reason) {
        log.info("OrderService::requestReturn - Execution started. [id: {}]", id);
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
        
        log.info("OrderService::requestReturn - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long id, PaymentStatus status) {
        log.info("OrderService::updatePaymentStatus - Execution started. [id: {}, status: {}]", id, status);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        PaymentStatus oldPaymentStatus = order.getPaymentStatus();
        order.setPaymentStatus(status);
        if (status == PaymentStatus.PAID) {
            order.setPaymentDate(Instant.now());
        }
        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, order.getStatus(), oldPaymentStatus, "Payment status updated");
        
        log.info("OrderService::updatePaymentStatus - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse updateShippingInfo(Long id, String carrierName, String carrierCode) {
        log.info("OrderService::updateShippingInfo - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        order.setCarrierName(carrierName);
        order.setCarrierServiceName(carrierCode);

        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, saved.getStatus(), saved.getPaymentStatus(), "Shipping info updated");
        
        log.info("OrderService::updateShippingInfo - Execution completed.");
        return orderMapper.toDto(saved);
    }

    // Method utils
    private void handleShippingInfo(Order order, OrderRequest request) {
        if (request.getOrderType() == OrderType.POS_INSTORE) {
            order.setShippingFee(BigDecimal.ZERO);
            return;
        }

        if (request.getAddressId() != null) {
            Long currentUserId = (request.getOrderType() == OrderType.ONLINE) ? getUserId()
                    : request.getUserId();

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

    private void processOrderItems(Order order, List<OrderItemRequest> items) {

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

            if (order.getOrderType() == OrderType.POS_INSTORE) {
                productVariantService.directDeductStock(variant.getId(), itemReq.getQuantity());
            } else {
                productVariantService.reserveStock(variant.getId(), itemReq.getQuantity());
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
                    .price(variant.getPrice())
                    .quantity(itemReq.getQuantity())
                    .build();
            details.add(detail);

            BigDecimal lineTotal = detail.getSubtotal();
            subtotal = subtotal.add(lineTotal);
        }

        order.setDetails(details);
        order.setSubtotal(subtotal);
    }

    private void calculateFinancials(Order order, OrderRequest request) {

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

    private void configurePaymentAndStatus(Order order, OrderRequest request) {

        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(
                        () -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Payment method not found"));

        order.setPaymentMethod(paymentMethod);
        order.setPaymentMethodName(paymentMethod.getName());

        if (request.getOrderType() == OrderType.POS_INSTORE) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentDate(Instant.now());
            order.setStatus(OrderStatus.COMPLETED);
        } else {
            order.setStatus(OrderStatus.PENDING);

            if (request.getTransactionId() != null && !request.getTransactionId().isBlank()) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setTransactionId(request.getTransactionId());
                order.setPaymentDate(Instant.now());
            } else {
                order.setPaymentStatus(PaymentStatus.UNPAID);
            }
        }
    }

    @Transactional
    public OrderResponse confirmOrder(Long id) {
        log.info("OrderService::confirmOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be PENDING to confirm");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Order confirmed");
        
        log.info("OrderService::confirmOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse shipOrder(Long id) {
        log.info("OrderService::shipOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.CONFIRMED && order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be CONFIRMED or PENDING to ship");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.SHIPPING);
        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Order shipped");
        
        log.info("OrderService::shipOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse completeOrder(Long id) {
        log.info("OrderService::completeOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order is already finished");
        }

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Commit reserved stock for ONLINE orders
        if (order.getOrderType() != OrderType.POS_INSTORE) {
            productVariantService.commitReservedStock(order.getDetails());

            // If COD, mark as PAID upon completion
            if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
                order.setPaymentStatus(PaymentStatus.PAID);
                order.setPaymentDate(Instant.now());
            }
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "Order completed");
        
        log.info("OrderService::completeOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("OrderService::cancelOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED || order.getStatus() == OrderStatus.CANCELLED
                || order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order is already finished");
        }

        if (order.getStatus() == OrderStatus.SHIPPING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Cannot cancel order while it is being shipped");
        }

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Release reserved stock for ONLINE orders
        if (order.getOrderType() != OrderType.POS_INSTORE) {
            productVariantService.releaseReservedStock(order.getDetails());

            if (order.getPaymentStatus() == PaymentStatus.PAID) {
                order.setPaymentStatus(PaymentStatus.REFUNDED); // Or keep PAID and handle refund separately
            }
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "Order cancelled");
        
        log.info("OrderService::cancelOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    // Validation/Calculate methods

    private void validateCoupon(Coupon coupon, BigDecimal subtotal) {
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

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
        BigDecimal discount = BigDecimal.ZERO;
        if (coupon.getType() == com.threadcity.jacketshopbackend.common.Enums.CouponType.AMOUNT) {
            discount = coupon.getValue();
        } else if (coupon.getType() == com.threadcity.jacketshopbackend.common.Enums.CouponType.PERCENT) {
            discount = subtotal.multiply(coupon.getValue().divide(new BigDecimal(100))); // Sửa lỗi chia 101 -> 100
            if (coupon.getMaxDiscount() != null && discount.compareTo(coupon.getMaxDiscount()) > 0) {
                discount = coupon.getMaxDiscount();
            }
        }
        return discount.min(subtotal);
    }

    private String generateOrderCode() {
        LocalDateTime now = LocalDateTime.now();
        String datePart = now.format(DateTimeFormatter.ofPattern("yyMMddHHmm"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "ORD" + datePart + randomPart;
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }

}

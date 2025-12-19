package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.*;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final ShippingMethodsRepository shippingMethodRepository;
    private final CouponRepository couponRepository;
    private final ProvinceRepository provinceRepository;
    private final DistrictRepository districtRepository;
    private final WardRepository wardRepository;
    private final OrderMapper orderMapper;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("OrderService::createOrder - Execution started.");
        Long userId = getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Cart is empty"));

        if (cart.getItems().isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Cart is empty");
        }

        // 1. Basic Info
        PaymentMethod paymentMethod = paymentMethodRepository.findById(request.getPaymentMethodId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Payment method not found"));
        ShippingMethod shippingMethod = shippingMethodRepository.findById(request.getShippingMethodId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED, "Shipping method not found"));

        Order order = Order.builder()
                .orderCode(generateOrderCode())
                .user(user)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .shippingRecipientName(request.getShippingRecipientName())
                .shippingRecipientPhone(request.getShippingRecipientPhone())
                .shippingAddressLine(request.getShippingAddressLine())
                .shippingProvinceCode(request.getShippingProvinceCode())
                .shippingDistrictCode(request.getShippingDistrictCode())
                .shippingWardCode(request.getShippingWardCode())
                .paymentMethod(paymentMethod)
                .paymentMethodName(paymentMethod.getName())
                .paymentStatus(PaymentStatus.UNPAID)
                .shippingMethod(shippingMethod)
                .shippingMethodName(shippingMethod.getName())
                .shippingFee(shippingMethod.getFee())
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .build();

        // Resolve location names
        if (request.getShippingProvinceCode() != null) {
            provinceRepository.findByGoshipId(Long.parseLong(request.getShippingProvinceCode()))
                    .ifPresent(p -> order.setShippingProvinceName(p.getName()));
        }
        if (request.getShippingDistrictCode() != null) {
            districtRepository.findByGoshipId(Long.parseLong(request.getShippingDistrictCode()))
                    .ifPresent(d -> order.setShippingDistrictName(d.getName()));
        }
        if (request.getShippingWardCode() != null) {
            wardRepository.findByGoshipId(Long.parseLong(request.getShippingWardCode()))
                    .ifPresent(w -> order.setShippingWardName(w.getName()));
        }

        // 2. Details and Inventory Check
        List<OrderDetail> details = cart.getItems().stream().map(cartItem -> {
            ProductVariant variant = cartItem.getProductVariant();
            
            if (variant.getQuantity() < cartItem.getQuantity()) {
                throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Not enough stock for: " + variant.getSku());
            }

            // Update inventory
            variant.setQuantity(variant.getQuantity() - cartItem.getQuantity());
            variant.setSoldCount(variant.getSoldCount() + cartItem.getQuantity());
            productVariantRepository.save(variant);

            return OrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .size(variant.getSize().getName())
                    .color(variant.getColor().getName())
                    .sku(variant.getSku())
                    .price(variant.getPrice())
                    .quantity(cartItem.getQuantity())
                    .build();
        }).collect(Collectors.toList());

        order.setDetails(details);

        // 3. Subtotal
        BigDecimal subtotal = details.stream()
                .map(d -> d.getPrice().multiply(new BigDecimal(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);

        // 4. Coupon
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            Coupon coupon = couponRepository.findByCode(request.getCouponCode())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupon not found"));
            
            validateCoupon(coupon, subtotal);

            order.setCoupon(coupon);
            order.setCouponCode(coupon.getCode());
            BigDecimal discount = calculateDiscount(coupon, subtotal);
            order.setDiscount(discount);
            coupon.setUsedCount(coupon.getUsedCount() + 1);
            couponRepository.save(coupon);
        }

        // 5. Total
        order.setTotal(subtotal.add(order.getShippingFee()).subtract(order.getDiscount()));

        Order savedOrder = orderRepository.save(order);

        // 6. Clear Cart
        cart.getItems().clear();
        cartRepository.save(cart);

        log.info("OrderService::createOrder - Execution completed.");
        return orderMapper.toDto(savedOrder);
    }

    public List<OrderResponse> getMyOrders() {
        log.info("OrderService::getMyOrders - Execution started.");
        Long userId = getUserId();
        List<Order> orders = orderRepository.findByUserId(userId);
        log.info("OrderService::getMyOrders - Execution completed.");
        return orders.stream().map(orderMapper::toDto).toList();
    }

    public OrderResponse getOrderById(Long id) {
        log.info("OrderService::getOrderById - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Order not found"));
        log.info("OrderService::getOrderById - Execution completed.");
        return orderMapper.toDto(order);
    }

    private void validateCoupon(Coupon coupon, BigDecimal subtotal) {
        if (coupon.getStatus() != com.threadcity.jacketshopbackend.common.Enums.Status.ACTIVE) {
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
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Minimum order value not met for this coupon");
        }
    }

    private BigDecimal calculateDiscount(Coupon coupon, BigDecimal subtotal) {
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

package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.Coupon;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import com.threadcity.jacketshopbackend.entity.ProductVariant;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PosOrderService extends AbstractOrderService {

    public PosOrderService(OrderRepository orderRepository,
            ProductVariantRepository productVariantRepository,
            PaymentMethodRepository paymentMethodRepository,
            CouponRepository couponRepository,
            UserRepository userRepository,
            AddressRepository addressRepository,
            OrderHistoryRepository orderHistoryRepository,
            CartService cartService,
            ProductVariantService productVariantService,
            OrderMapper orderMapper) {
        super(orderRepository, productVariantRepository, paymentMethodRepository, couponRepository,
                userRepository, addressRepository, orderHistoryRepository, cartService, productVariantService,
                orderMapper);
    }

    @Override
    protected void handleStockForCreate(Long variantId, Integer quantity) {
        productVariantService.directDeductStock(variantId, quantity);
    }

    @Transactional
    public OrderResponse createPosDraft(OrderRequest request) {
        log.info("PosOrderService::createPosDraft - Start");

        // Count BOTH POS_INSTORE and POS_DELIVERY pending orders (store-wide, not per staff)
        long countInstore = orderRepository.countByOrderTypeAndStatus(OrderType.POS_INSTORE, OrderStatus.PENDING);
        long countDelivery = orderRepository.countByOrderTypeAndStatus(OrderType.POS_DELIVERY, OrderStatus.PENDING);
        long totalPosPending = countInstore + countDelivery;

        if (totalPosPending >= 5) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                "Maximum 5 pending POS orders allowed. Please complete or cancel existing drafts.");
        }

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(request.getOrderType());
        order.setNote(request.getNote());

        // Status PENDING, Payment UNPAID
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);

        // User/Customer logic - Optional for Draft
        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "Member not found"));
            order.setUser(user);
            // Default to user info if not explicitly provided
            if (request.getCustomerName() == null) order.setCustomerName(user.getFullName());
            else order.setCustomerName(request.getCustomerName());

            if (request.getCustomerPhone() == null) order.setCustomerPhone(user.getPhone());
            else order.setCustomerPhone(request.getCustomerPhone());
        } else {
            // No user, just set explicit name/phone if provided
            order.setCustomerName(request.getCustomerName());
            order.setCustomerPhone(request.getCustomerPhone());
        }

        // Staff
        Long staffId = getUserId();
        User staff = userRepository.getReferenceById(staffId);
        order.setStaff(staff);

        handleShippingInfo(order, request);

        // Process items WITHOUT stock deduction
        List<OrderItemRequest> items = request.getItems() != null ? request.getItems() : new ArrayList<>();
        processOrderItems(order, items, false);

        if (order.getDetails().isEmpty()) {
            log.warn("PosOrderService::createPosDraft - Created empty draft [Code: {}]", order.getOrderCode());
        }

        calculateFinancials(order, request);
        
        if (request.getPaymentMethodId() != null) {
            configurePaymentAndStatus(order, request); // Will call super, sets method. Status kept PENDING.
        }

        Order savedOrder = orderRepository.save(order);
        saveOrderHistory(savedOrder, null, null, "POS draft created");

        log.info("PosOrderService::createPosDraft - Success [Code: {}]", savedOrder.getOrderCode());
        return orderMapper.toDto(savedOrder);
    }

    @Transactional
    public OrderResponse completePosOrder(Long id) {
        log.info("PosOrderService::completePosOrder - Start [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be PENDING to complete");
        }

        // Validate before complete
        if (order.getDetails().isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Draft must have at least 1 item");
        }
        if (order.getCustomerName() == null || order.getCustomerName().trim().isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Customer name required");
        }

        // Deduct stock
        for (OrderDetail detail : order.getDetails()) {
            productVariantService.directDeductStock(detail.getProductVariant().getId(), detail.getQuantity());
        }

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentDate(Instant.now());

        incrementCouponUsage(order.getCouponCode());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "POS order completed");

        log.info("PosOrderService::completePosOrder - Success");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse updatePosDraft(Long id, OrderRequest request) {
        log.info("PosOrderService::updatePosDraft - Start [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be PENDING to update");
        }

        order.setNote(request.getNote());

        if (request.getCustomerName() != null)
            order.setCustomerName(request.getCustomerName());
        if (request.getCustomerPhone() != null)
            order.setCustomerPhone(request.getCustomerPhone());

        handleShippingInfo(order, request);

        // Re-process items (replace list) - without stock deduction
        if (request.getItems() != null) {
            processOrderItems(order, request.getItems(), false);
        }

        calculateFinancials(order, request);
        
        // Re-validate coupon if exists
        if (order.getCouponCode() != null) {
            Coupon coupon = couponRepository.findByCode(order.getCouponCode())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupon not found"));
            validateCoupon(coupon, order.getSubtotal());
        }
        
        if (request.getPaymentMethodId() != null) {
            configurePaymentAndStatus(order, request); // Updates payment method
        }

        Order saved = orderRepository.save(order);
        // saveOrderHistory(saved, saved.getStatus(), saved.getPaymentStatus(), "Draft updated"); // Optional

        log.info("PosOrderService::updatePosDraft - Success");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public void cancelPosDraft(Long id) {
        log.info("PosOrderService::cancelPosDraft - Start [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be PENDING to cancel");
        }
        
        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "Draft cancelled");
        
        log.info("PosOrderService::cancelPosDraft - Success");
    }

    @Transactional
    public OrderResponse addItemToDraft(Long draftId, OrderItemRequest itemRequest) {
        log.info("PosOrderService::addItemToDraft - Start [draftId: {}, variantId: {}]",
                draftId, itemRequest.getProductVariantId());

        Order order = orderRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        // Get product variant and validate
        ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "Product variant not found"));

        if (variant.getStatus() != Status.ACTIVE) {
            throw new InvalidRequestException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                    "Product " + variant.getProduct().getName() + " is currently unavailable");
        }

        // Check if variant already exists in draft
        Optional<OrderDetail> existingDetail = order.getDetails().stream()
                .filter(d -> d.getProductVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingDetail.isPresent()) {
            // Item already in draft -> increase quantity
            OrderDetail detail = existingDetail.get();
            int newQuantity = detail.getQuantity() + itemRequest.getQuantity();
            detail.setQuantity(newQuantity);
            log.info("PosOrderService::addItemToDraft - Item already exists, increased quantity to {}", newQuantity);
        } else {
            // New item -> create and add to list
            OrderDetail newDetail = OrderDetail.builder()
                    .order(order)
                    .productVariant(variant)
                    .productName(variant.getProduct().getName())
                    .sku(variant.getSku())
                    .size(variant.getSize().getName())
                    .color(variant.getColor().getName())
                    .material(variant.getMaterial().getName())
                    .image(variant.getImage())
                    .price(variant.getPrice())
                    .quantity(itemRequest.getQuantity())
                    .build();
            order.getDetails().add(newDetail);
            log.info("PosOrderService::addItemToDraft - New item added with quantity {}", itemRequest.getQuantity());
        }

        // Recalculate all financials
        recalculateDraftFinancials(order);

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::addItemToDraft - Success [total items: {}]", saved.getDetails().size());
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse updateDraftItemQuantity(Long draftId, Long itemId, Integer quantity) {
        log.info("PosOrderService::updateDraftItemQuantity - Start [draftId: {}, itemId: {}, quantity: {}]",
                draftId, itemId, quantity);

        Order order = orderRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        // Find the specific item
        OrderDetail item = order.getDetails().stream()
                .filter(d -> d.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED,
                        "Item not found in this draft"));

        if (quantity <= 0) {
            // Quantity = 0 -> remove item
            order.getDetails().remove(item);
            log.info("PosOrderService::updateDraftItemQuantity - Item removed (quantity = 0)");

            // Check if draft is now empty
            if (order.getDetails().isEmpty()) {
                order.setStatus(OrderStatus.CANCELLED);
                orderRepository.save(order);
                saveOrderHistory(order, OrderStatus.PENDING, order.getPaymentStatus(),
                        "Draft auto-cancelled: all items removed");
                throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                        "Draft cancelled: no items remaining");
            }
        } else {
            // Update quantity
            item.setQuantity(quantity);
            log.info("PosOrderService::updateDraftItemQuantity - Quantity updated to {}", quantity);
        }

        recalculateDraftFinancials(order);

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::updateDraftItemQuantity - Success");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse removeItemFromDraft(Long draftId, Long itemId) {
        log.info("PosOrderService::removeItemFromDraft - Start [draftId: {}, itemId: {}]", draftId, itemId);

        Order order = orderRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        // Remove item
        boolean removed = order.getDetails().removeIf(d -> d.getId().equals(itemId));
        if (!removed) {
            throw new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED,
                    "Item not found in this draft");
        }

        log.info("PosOrderService::removeItemFromDraft - Item removed");

        // Check if draft is now empty
        if (order.getDetails().isEmpty()) {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
            saveOrderHistory(order, OrderStatus.PENDING, order.getPaymentStatus(),
                    "Draft auto-cancelled: all items removed");
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                    "Draft cancelled: no items remaining");
        }

        recalculateDraftFinancials(order);

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::removeItemFromDraft - Success [remaining items: {}]", saved.getDetails().size());
        return orderMapper.toDto(saved);
    }

    /**
     * Recalculate subtotal, discount, and total after item changes
     */
    private void recalculateDraftFinancials(Order order) {
        // 1. Recalculate subtotal from all items
        BigDecimal subtotal = order.getDetails().stream()
                .map(detail -> detail.getPrice().multiply(new BigDecimal(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);

        // 2. Re-validate coupon and recalculate discount
        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            try {
                Coupon coupon = couponRepository.findByCode(order.getCouponCode())
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.COUPON_NOT_FOUND, "Coupon not found"));

                // Validate coupon against new subtotal
                validateCoupon(coupon, subtotal);

                // Recalculate discount
                BigDecimal discount = calculateDiscount(coupon, subtotal);
                order.setDiscount(discount);

                log.info("PosOrderService::recalculateDraftFinancials - Coupon applied: {} discount", discount);
            } catch (Exception e) {
                // Coupon no longer valid (e.g., min order value not met) -> remove it
                log.warn("PosOrderService::recalculateDraftFinancials - Coupon invalid, removing: {}", e.getMessage());
                order.setCouponCode(null);
                order.setDiscount(BigDecimal.ZERO);
            }
        } else {
            order.setDiscount(BigDecimal.ZERO);
        }

        // 3. Recalculate total
        BigDecimal total = subtotal
                .add(order.getShippingFee() != null ? order.getShippingFee() : BigDecimal.ZERO)
                .subtract(order.getDiscount());

        if (total.compareTo(BigDecimal.ZERO) < 0) {
            total = BigDecimal.ZERO;
        }
        order.setTotal(total);

        log.info("PosOrderService::recalculateDraftFinancials - Subtotal: {}, Discount: {}, Total: {}",
                subtotal, order.getDiscount(), total);
    }

    // Legacy instant create
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("PosOrderService::createOrder (Instant) - Start");

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(request.getOrderType());
        order.setNote(request.getNote());

        if (request.getUserId() != null) {
            User user = userRepository.findById(request.getUserId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "Member not found"));
            order.setUser(user);
            order.setCustomerName(user.getFullName());
            order.setCustomerPhone(user.getPhone());
        } else {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                    "User (Customer) is required for POS order");
        }

        Long staffId = getUserId();
        User staff = userRepository.getReferenceById(staffId);
        order.setStaff(staff);

        handleShippingInfo(order, request);
        processOrderItems(order, request.getItems(), true); // Deduct stock immediately
        calculateFinancials(order, request);

        configurePaymentAndStatus(order, request); // Sets method, keeps PENDING default

        // Override to COMPLETED for instant flow
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentDate(Instant.now());

        Order savedOrder = orderRepository.save(order);
        incrementCouponUsage(request.getCouponCode());
        saveOrderHistory(savedOrder, null, null, "Order created (POS Instant)");

        log.info("PosOrderService::createOrder - Success [Code: {}]", savedOrder.getOrderCode());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    protected void configurePaymentAndStatus(Order order, OrderRequest request) {
        super.configurePaymentAndStatus(order, request);
    }

    @Transactional
    public OrderResponse confirmOrder(Long id) {
        throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                "POS orders cannot be confirmed via this flow");
    }

    @Transactional
    public OrderResponse shipOrder(Long id) {
        throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                "POS orders cannot be shipped via this flow");
    }

    @Transactional
    public OrderResponse completeOrder(Long id) {
        throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                "Use completePosOrder() for POS orders");
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                "Use cancelPosDraft() for POS orders");
    }
}

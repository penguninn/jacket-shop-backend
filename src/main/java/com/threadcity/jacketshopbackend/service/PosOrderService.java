package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.order.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.order.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.order.request.UpdatePaymentRequest;
import com.threadcity.jacketshopbackend.dto.order.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.*;
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
import java.util.Optional;

@Service
@Slf4j
public class PosOrderService extends AbstractOrderService {

    private static final int MAX_PENDING_DRAFTS = 5;

    public PosOrderService(
            OrderRepository orderRepository,
            ProductVariantRepository productVariantRepository,
            PaymentMethodRepository paymentMethodRepository,
            UserRepository userRepository,
            AddressRepository addressRepository,
            OrderHistoryRepository orderHistoryRepository,
            StockService stockService,
            PricingService pricingService,
            CouponService couponService,
            CartService cartService,
            OrderMapper orderMapper) {
        super(orderRepository, productVariantRepository, paymentMethodRepository,
                userRepository, addressRepository, orderHistoryRepository,
                stockService, pricingService, couponService,
                cartService, orderMapper);
    }

    @Transactional
    public OrderResponse createPosDraft() {
        log.info("PosOrderService::createPosDraft - Start");

        long pendingCount = orderRepository.countByOrderTypeAndStatus(OrderType.POS_INSTORE, OrderStatus.PENDING);
        if (pendingCount >= MAX_PENDING_DRAFTS) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                    "Maximum " + MAX_PENDING_DRAFTS + " pending POS drafts allowed. Please complete or cancel existing drafts.");
        }

        // Build order
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(OrderType.POS_INSTORE);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);

        // Set staff
        Long staffId = getUserId();
        User staff = userRepository.getReferenceById(staffId);
        order.setStaff(staff);
        order.setStaffName(staff.getFullName());

        // Set customer default
        User customerDefault = userRepository.findByUsername("guest")
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.USER_NOT_FOUND,
                        "Customer default not found")
                );
        order.setUser(customerDefault);
        order.setCustomerName(customerDefault.getFullName());
        order.setCustomerPhone(customerDefault.getPhone());

        // Set payment method default
        PaymentMethod paymentMethodDefault = paymentMethodRepository.findByCode("QR_INSTORE")
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PAYMENT_METHOD_NOT_FOUND, "Payment method not found"));
        order.setPaymentMethod(paymentMethodDefault);
        order.setPaymentMethodCode(paymentMethodDefault.getCode());
        order.setPaymentMethodName(paymentMethodDefault.getName());

        // Initialize financials
        order.setShippingFee(BigDecimal.ZERO);
        order.setSubtotal(BigDecimal.ZERO);
        order.setDiscount(BigDecimal.ZERO);
        order.setTotal(BigDecimal.ZERO);

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, null, null, "POS draft created");

        log.info("PosOrderService::createPosDraft - Success [Code: {}]", saved.getOrderCode());
        return orderMapper.toDto(saved);
    }

    /**
     * Complete POS order: PENDING -> COMPLETED
     * Commits reserved stock and marks as PAID.
     */
    @Transactional
    public OrderResponse completePosOrder(Long id) {
        log.info("PosOrderService::completePosOrder - Start [id: {}]", id);

        Order order = getDraftOrder(id);

        // Validate
        if (order.getOrderDetails().isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Draft must have at least 1 item");
        }
        if (order.getCustomerName() == null || order.getCustomerName().isBlank()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Customer name is required");
        }

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Commit reserved stock
        stockService.commitReservedStock(new ArrayList<>(order.getOrderDetails()));

        // Increment coupon usage
        couponService.incrementUsage(order.getCouponCode());

        // Update status
        order.setStatus(OrderStatus.COMPLETED);
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentDate(Instant.now());
        order.setCompletedAt(Instant.now());

        // Set staff
        Long staffId = getUserId();
        User staff = userRepository.getReferenceById(staffId);
        order.setStaff(staff);
        order.setStaffName(staff.getFullName());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "POS order completed");

        log.info("PosOrderService::completePosOrder - Success");
        return orderMapper.toDto(saved);
    }

    /**
     * Cancel POS draft: PENDING -> CANCELLED
     * Releases all reserved stock.
     */
    @Transactional
    public void cancelPosDraft(Long id) {
        log.info("PosOrderService::cancelPosDraft - Start [id: {}]", id);

        Order order = getDraftOrder(id);

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Release all reserved stock
        if (!order.getOrderDetails().isEmpty()) {
            stockService.releaseReservedStock(new ArrayList<>(order.getOrderDetails()));
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "POS draft cancelled");

        log.info("PosOrderService::cancelPosDraft - Success");
    }

    // ==================== ITEM MANAGEMENT ====================

    /**
     * Add item to draft.
     * Reserves stock immediately to prevent overselling.
     */
    @Transactional
    public OrderResponse addItemToDraft(Long draftId, OrderItemRequest itemRequest) {
        log.info("PosOrderService::addItemToDraft - Start [draftId: {}, variantId: {}]",
                draftId, itemRequest.getProductVariantId());

        Order order = getDraftOrder(draftId);

        // Get and validate variant
        ProductVariant variant = productVariantRepository.findById(itemRequest.getProductVariantId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                        "Product variant not found"));

        if (variant.getStatus() != Status.ACTIVE) {
            throw new InvalidRequestException(ErrorCodes.PRODUCT_VARIANT_NOT_FOUND,
                    "Product " + variant.getProduct().getName() + " is currently unavailable");
        }

        // Reserve stock
        stockService.reserveStock(variant.getId(), itemRequest.getQuantity());

        // Calculate pricing
        PricingService.PriceResult priceResult = pricingService.calculatePrice(variant);

        // Check if variant already exists in draft
        Optional<OrderDetail> existingDetail = order.getOrderDetails().stream()
                .filter(d -> d.getProductVariant().getId().equals(variant.getId()))
                .findFirst();

        if (existingDetail.isPresent()) {
            // Update existing detail
            OrderDetail detail = existingDetail.get();
            int newQuantity = detail.getQuantity() + itemRequest.getQuantity();
            detail.setQuantity(newQuantity);
            detail.setPrice(priceResult.getFinalPrice());
            detail.setOriginalPrice(priceResult.getOriginalPrice());
            detail.setDiscountPercentage(priceResult.getDiscountPercentage());
            log.info("PosOrderService::addItemToDraft - Item exists, increased quantity to {}", newQuantity);
        } else {
            // Create new detail
            OrderDetail newDetail = OrderDetail.builder()
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
                    .quantity(itemRequest.getQuantity())
                    .subtotal(variant.getPrice().multiply(new BigDecimal(itemRequest.getQuantity())))
                    .build();
            order.getOrderDetails().add(newDetail);
            log.info("PosOrderService::addItemToDraft - New item added with quantity {}", itemRequest.getQuantity());
        }

        recalculateDraftFinancials(order);

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::addItemToDraft - Success [total items: {}]", saved.getOrderDetails().size());
        return orderMapper.toDto(saved);
    }

    /**
     * Update item quantity in draft.
     * Adjusts reserved stock accordingly.
     */
    @Transactional
    public OrderResponse updateDraftItemQuantity(Long draftId, Long itemId, Integer newQuantity) {
        log.info("PosOrderService::updateDraftItemQuantity - Start [draftId: {}, itemId: {}, quantity: {}]",
                draftId, itemId, newQuantity);

        Order order = getDraftOrder(draftId);

        // Find item
        OrderDetail item = order.getOrderDetails().stream()
                .filter(d -> d.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED,
                        "Item not found in this draft"));

        int oldQuantity = item.getQuantity();

        if (newQuantity <= 0) {
            // Remove item
            stockService.releaseReservedStock(item.getProductVariant().getId(), oldQuantity);
            order.getOrderDetails().remove(item);
            log.info("PosOrderService::updateDraftItemQuantity - Item removed (quantity <= 0)");

            // Check if draft is now empty
            if (order.getOrderDetails().isEmpty()) {
                order.setStatus(OrderStatus.CANCELLED);
                order.setCancelledAt(Instant.now());
                orderRepository.save(order);
                saveOrderHistory(order, OrderStatus.PENDING, order.getPaymentStatus(),
                        "Draft auto-cancelled: all items removed");
                throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED,
                        "Draft cancelled: no items remaining");
            }
        } else {
            // Adjust reserved stock
            stockService.adjustReservedStock(item.getProductVariant().getId(), oldQuantity, newQuantity);

            // Update quantity and refresh pricing
            item.setQuantity(newQuantity);
            PricingService.PriceResult priceResult = pricingService.calculatePrice(item.getProductVariant());
            item.setPrice(priceResult.getFinalPrice());
            item.setOriginalPrice(priceResult.getOriginalPrice());
            item.setDiscountPercentage(priceResult.getDiscountPercentage());

            log.info("PosOrderService::updateDraftItemQuantity - Quantity updated from {} to {}", oldQuantity, newQuantity);
        }

        recalculateDraftFinancials(order);

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::updateDraftItemQuantity - Success");
        return orderMapper.toDto(saved);
    }

    /**
     * Remove item from draft.
     * Releases reserved stock.
     */
    @Transactional
    public OrderResponse removeItemFromDraft(Long draftId, Long itemId) {
        log.info("PosOrderService::removeItemFromDraft - Start [draftId: {}, itemId: {}]", draftId, itemId);

        Order order = getDraftOrder(draftId);

        // Find item
        OrderDetail item = order.getOrderDetails().stream()
                .filter(d -> d.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.VALIDATION_FAILED,
                        "Item not found in this draft"));

        // Release reserved stock
        stockService.releaseReservedStock(item.getProductVariant().getId(), item.getQuantity());

        // Remove item
        order.getOrderDetails().remove(item);
        log.info("PosOrderService::removeItemFromDraft - Item removed");

        recalculateDraftFinancials(order);

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::removeItemFromDraft - Success [remaining items: {}]", saved.getOrderDetails().size());
        return orderMapper.toDto(saved);
    }

    // ==================== DRAFT UPDATE METHODS ====================

    /**
     * Update draft general info (note).
     */
    @Transactional
    public OrderResponse updatePosDraftInfo(Long id, OrderRequest request) {
        log.info("PosOrderService::updatePosDraftInfo - Start [id: {}]", id);

        Order order = getDraftOrder(id);
        order.setNote(request.getNote());

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::updatePosDraftInfo - Success");
        return orderMapper.toDto(saved);
    }

    /**
     * Update draft customer info.
     */
    @Transactional
    public OrderResponse updatePosDraftCustomer(Long draftId, Long customerId) {
        log.info("PosOrderService::updatePosDraftCustomer - Start [id: {}]", draftId);

        Order order = getDraftOrder(draftId);
        if (customerId != null) {
            if (order.getUser() == null || !customerId.equals(order.getUser().getId())) {
                User customer = userRepository.findById(customerId)
                        .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "Customer not found"));
                order.setUser(customer);
                order.setCustomerEmail(customer.getEmail());
                order.setCustomerName(customer.getFullName());
                order.setCustomerPhone(customer.getPhone());
            }
        }

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::updatePosDraftCustomer - Success");
        return orderMapper.toDto(saved);
    }

    /**
     * Update draft coupon.
     */
    @Transactional
    public OrderResponse updatePosDraftCoupon(Long id, String couponCode) {
        log.info("PosOrderService::updatePosDraftCoupon - Start [id: {}]", id);

        Order order = getDraftOrder(id);
        order.setCouponCode(couponCode);
        recalculateDraftFinancials(order);

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::updatePosDraftCoupon - Success");
        return orderMapper.toDto(saved);
    }

    /**
     * Update draft payment method.
     */
    @Transactional
    public OrderResponse updatePosPayment(Long id, UpdatePaymentRequest request) {
        log.info("PosOrderService::updatePosPayment - Start [id: {}]", id);

        Order order = getDraftOrder(id);

        if (request.getPaymentMethodId() != null) {
            configurePaymentMethod(order, request.getPaymentMethodId());
        }

        Order saved = orderRepository.save(order);
        log.info("PosOrderService::updatePosPayment - Success");
        return orderMapper.toDto(saved);
    }

    // ==================== HELPER METHODS ====================

    /**
     * Get draft order by ID. Validates that it is PENDING.
     */
    private Order getDraftOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be PENDING to update");
        }

        return order;
    }

    /**
     * Recalculate subtotal, discount, and total after item changes.
     */
    private void recalculateDraftFinancials(Order order) {
        // 1. Recalculate subtotal
        BigDecimal subtotal = order.getOrderDetails().stream()
                .map(d -> d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setSubtotal(subtotal);

        // 2. Validate and calculate coupon discount
        BigDecimal discount = BigDecimal.ZERO;
        if (order.getCouponCode() != null && !order.getCouponCode().isBlank()) {
            Coupon coupon = couponService.findAndValidate(order.getCouponCode(), subtotal);
            if (coupon != null) {
                discount = coupon.getValue();
                order.setCoupon(coupon);
            }
        }
        order.setDiscount(discount);

        // 3. Recalculate total
        recalculateTotal(order);
        log.debug("PosOrderService::recalculateDraftFinancials - Subtotal: {}, Discount: {}, Total: {}",
                subtotal, discount, order.getTotal());
    }
}

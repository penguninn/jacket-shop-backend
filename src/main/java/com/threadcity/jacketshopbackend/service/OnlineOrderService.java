package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.dto.cart.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.order.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.order.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import com.threadcity.jacketshopbackend.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;

/**
 * Service for Online Orders.
 *
 * Flow: PENDING -> CONFIRMED -> SHIPPING -> COMPLETED
 *
 * Stock Management:
 * - CREATE: reserveStock (available--, reserved++)
 * - CANCEL: releaseReserved (available++, reserved--)
 * - COMPLETE: commitReserved (reserved--)
 * - RETURN APPROVED: returnStock (available++)
 */
@Service
@Slf4j
public class OnlineOrderService extends AbstractOrderService {

    public OnlineOrderService(
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

    // ==================== CREATE ORDER ====================

    /**
     * Create new online order.
     *
     * Steps:
     * 1. Build order entity with customer info
     * 2. Handle shipping info from address or manual fields
     * 3. Process items (validate, reserve stock, calculate pricing)
     * 4. Calculate financials (coupon discount, total)
     * 5. Configure payment method
     * 6. Save order and history
     * 7. Increment coupon usage
     * 8. Clear user's cart
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("OnlineOrderService::createOrder - Start");

        // 1. Build order
        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(OrderType.ONLINE);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
        order.setNote(request.getNote());

        // Set customer info from current user
        Long userId = getUserId();
        User user = userRepository.getReferenceById(userId);
        order.setUser(user);
        order.setCustomerName(user.getFullName());
        order.setCustomerPhone(user.getPhone());
        order.setCustomerEmail(user.getEmail());

        // 2. Handle shipping info
        handleShippingInfo(order, request);

        // 3. Process items (reserve stock)
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidRequestException(ErrorCodes.VALIDATION_FAILED, "Order must have at least 1 item");
        }
        processOrderItems(order, request.getItems(), true);

        // 4. Calculate financials
        calculateFinancials(order, request.getCouponCode());

        // 5. Configure payment
        configurePaymentMethod(order, request.getPaymentMethodId());

        // 6. Save order
        Order savedOrder = orderRepository.save(order);
        saveOrderHistory(savedOrder, null, null, "Order created");

        // 7. Increment coupon usage
        couponService.incrementUsage(request.getCouponCode());

        // 8. Clear cart
        cartService.clearCart();

        log.info("OnlineOrderService::createOrder - Success [Code: {}]", savedOrder.getOrderCode());
        return orderMapper.toDto(savedOrder);
    }

    // ==================== ORDER STATE TRANSITIONS ====================

    /**
     * Confirm order: PENDING -> CONFIRMED
     */
    @Transactional
    public OrderResponse confirmOrder(Long id) {
        log.info("OnlineOrderService::confirmOrder - Start [id: {}]", id);

        Order order = findOrderById(id);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be PENDING to confirm");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);
        order.setConfirmedAt(Instant.now());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Order confirmed");

        log.info("OnlineOrderService::confirmOrder - Completed");
        return orderMapper.toDto(saved);
    }

    /**
     * Ship order: CONFIRMED -> SHIPPING
     */
    @Transactional
    public OrderResponse shipOrder(Long id) {
        log.info("OnlineOrderService::shipOrder - Start [id: {}]", id);

        Order order = findOrderById(id);

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be CONFIRMED to ship");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.SHIPPING);
        order.setShippedAt(Instant.now());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Order shipped");

        log.info("OnlineOrderService::shipOrder - Completed");
        return orderMapper.toDto(saved);
    }

    /**
     * Complete order: SHIPPING -> COMPLETED
     *
     * Commits reserved stock and marks COD orders as PAID.
     */
    @Transactional
    public OrderResponse completeOrder(Long id) {
        log.info("OnlineOrderService::completeOrder - Start [id: {}]", id);

        Order order = findOrderById(id);

        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be SHIPPING to complete");
        }

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Commit reserved stock
        stockService.commitReservedStock(new ArrayList<>(order.getOrderDetails()));

        // Mark COD as paid upon delivery
        if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentDate(Instant.now());
        }

        order.setStatus(OrderStatus.COMPLETED);
        order.setCompletedAt(Instant.now());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "Order completed");

        log.info("OnlineOrderService::completeOrder - Completed");
        return orderMapper.toDto(saved);
    }

    /**
     * Cancel order: PENDING/CONFIRMED -> CANCELLED
     *
     * Releases reserved stock and refunds if paid.
     */
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("OnlineOrderService::cancelOrder - Start [id: {}]", id);

        Order order = findOrderById(id);

        // Verify ownership
        SecurityUtils.requireOwnership(order.getUser().getId(), "order");

        // Validate status
        if (order.getStatus() == OrderStatus.COMPLETED ||
            order.getStatus() == OrderStatus.CANCELLED ||
            order.getStatus() == OrderStatus.RETURNED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order is already finished");
        }

        if (order.getStatus() == OrderStatus.SHIPPING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Cannot cancel order while it is being shipped");
        }

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Release reserved stock
        stockService.releaseReservedStock(new ArrayList<>(order.getOrderDetails()));

        // Decrement coupon usage
        couponService.decrementUsage(order.getCouponCode());

        // Refund if paid
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(Instant.now());

        Order saved = orderRepository.save(order);
        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "Order cancelled");

        log.info("OnlineOrderService::cancelOrder - Completed");
        return orderMapper.toDto(saved);
    }

    // ==================== CUSTOMER ACTIONS ====================

    /**
     * Customer receives order: SHIPPING -> COMPLETED
     */
    @Transactional
    public OrderResponse receiveOrder(Long id) {
        log.info("OnlineOrderService::receiveOrder - Start [id: {}]", id);

        Order order = findOrderById(id);

        // Verify ownership
        Long currentUserId = getUserId();
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be SHIPPING to mark as received");
        }

        // Delegate to completeOrder
        return completeOrder(id);
    }

    /**
     * Reorder: Add items from a previous order to cart.
     */
    @Transactional
    public void reorder(Long id) {
        log.info("OnlineOrderService::reorder - Start [id: {}]", id);

        Order order = findOrderById(id);

        // Verify ownership
        Long currentUserId = getUserId();
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only reorder your own orders");
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            try {
                CartItemRequest cartItemRequest = new CartItemRequest();
                cartItemRequest.setProductVariantId(detail.getProductVariant().getId());
                cartItemRequest.setQuantity(detail.getQuantity());
                cartService.addToCart(cartItemRequest);
            } catch (Exception e) {
                log.warn("Could not add item {} to cart during reorder: {}", detail.getSku(), e.getMessage());
            }
        }

        log.info("OnlineOrderService::reorder - Completed");
    }

    // ==================== PRIVATE HELPERS ====================

    private Order findOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));
    }
}

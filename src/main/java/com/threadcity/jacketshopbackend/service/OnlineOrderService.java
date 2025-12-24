package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.dto.request.CartItemRequest;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.OrderMapper;
import com.threadcity.jacketshopbackend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@Slf4j
public class OnlineOrderService extends AbstractOrderService {

    public OnlineOrderService(OrderRepository orderRepository,
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
        productVariantService.reserveStock(variantId, quantity);
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        log.info("OnlineOrderService::createOrder - Start");

        Order order = new Order();
        order.setOrderCode(generateOrderCode());
        order.setOrderType(OrderType.ONLINE);
        order.setNote(request.getNote());

        Long userId = getUserId();
        User user = userRepository.getReferenceById(userId);
        order.setUser(user);
        order.setCustomerName(user.getFullName());
        order.setCustomerPhone(user.getPhone());

        handleShippingInfo(order, request);
        processOrderItems(order, request.getItems());
        calculateFinancials(order, request);

        configurePaymentAndStatus(order, request);

        Order savedOrder = orderRepository.save(order);
        incrementCouponUsage(request.getCouponCode());
        saveOrderHistory(savedOrder, null, null, "Order created");

        cartService.clearCart();

        log.info("OnlineOrderService::createOrder - Success [Code: {}]", savedOrder.getOrderCode());
        return orderMapper.toDto(savedOrder);
    }

    @Override
    protected void configurePaymentAndStatus(Order order, OrderRequest request) {
        super.configurePaymentAndStatus(order, request);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus(PaymentStatus.UNPAID);
    }

    @Transactional
    public OrderResponse confirmOrder(Long id) {
        log.info("OnlineOrderService::confirmOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be PENDING to confirm");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.CONFIRMED);
        Order saved = orderRepository.save(order);

        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Order confirmed");

        log.info("OnlineOrderService::confirmOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse shipOrder(Long id) {
        log.info("OnlineOrderService::shipOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be CONFIRMED to ship");
        }

        OrderStatus oldStatus = order.getStatus();
        order.setStatus(OrderStatus.SHIPPING);
        Order saved = orderRepository.save(order);

        saveOrderHistory(saved, oldStatus, saved.getPaymentStatus(), "Order shipped");

        log.info("OnlineOrderService::shipOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse completeOrder(Long id) {
        log.info("OnlineOrderService::completeOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS, "Order must be SHIPPING to complete");
        }

        OrderStatus oldStatus = order.getStatus();
        PaymentStatus oldPaymentStatus = order.getPaymentStatus();

        // Commit reserved stock
        productVariantService.commitReservedStock(order.getDetails());

        // If COD, mark as PAID upon completion
        if (order.getPaymentStatus() == PaymentStatus.UNPAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentDate(Instant.now());
        }

        order.setStatus(OrderStatus.COMPLETED);
        Order saved = orderRepository.save(order);

        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "Order completed");

        log.info("OnlineOrderService::completeOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        log.info("OnlineOrderService::cancelOrder - Execution started. [id: {}]", id);
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

        // Release reserved stock
        productVariantService.releaseReservedStock(order.getDetails());

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.REFUNDED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        saveOrderHistory(saved, oldStatus, oldPaymentStatus, "Order cancelled");

        log.info("OnlineOrderService::cancelOrder - Execution completed.");
        return orderMapper.toDto(saved);
    }

    @Transactional
    public OrderResponse receiveOrder(Long id) {
        log.info("OnlineOrderService::receiveOrder - Execution started. [id: {}]", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        Long currentUserId = getUserId();
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Access denied");
        }

        // TIGHTENED: Only allow receiving when order is actually SHIPPING
        if (order.getStatus() != OrderStatus.SHIPPING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be SHIPPING to mark as received");
        }

        return completeOrder(id);
    }

    @Transactional
    public void reorder(Long id) {
        log.info("OnlineOrderService::reorder - Execution started. [id: {}]", id);
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
        log.info("OnlineOrderService::reorder - Execution completed.");
    }
}

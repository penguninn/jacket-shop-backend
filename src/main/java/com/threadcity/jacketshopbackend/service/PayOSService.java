package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.*;
import vn.payos.model.webhooks.WebhookData;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayOSService {

    private final PayOS payOS;
    private final OrderRepository orderRepository;
    private final StockService stockService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Transactional
    public CreatePaymentLinkResponse createPaymentLink(Long orderId) {
        log.info("PayOSService::createPaymentLink - Start [orderId: {}]", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "This order has been cancelled. Please create a new order.");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new InvalidRequestException(ErrorCodes.INVALID_REQUEST, "Order already paid");
        }

        if (order.getPayosOrderCode() != null && order.getPayosCheckoutUrl() != null) {
            try {
                PaymentLink existingLink = payOS.paymentRequests().get(order.getPayosOrderCode());

                if (existingLink != null) {
                    PaymentLinkStatus status = existingLink.getStatus();
                    log.info("PayOSService::createPaymentLink - Found existing link [payosOrderCode: {}, status: {}]",
                            order.getPayosOrderCode(), status);

                    if (status == PaymentLinkStatus.PENDING || status == PaymentLinkStatus.PROCESSING) {
                        log.info("PayOSService::createPaymentLink - Returning cached payment link");
                        return CreatePaymentLinkResponse.builder()
                                .orderCode(order.getPayosOrderCode())
                                .paymentLinkId(order.getPayosPaymentLinkId())
                                .checkoutUrl(order.getPayosCheckoutUrl())
                                .qrCode(order.getPayosQrCode())
                                .amount(order.getTotal().longValue())
                                .status(status)
                                .description("DH " + order.getOrderCode())
                                .currency("VND")
                                .bin("")
                                .accountNumber("")
                                .accountName("")
                                .build();
                    }

                    if (status == PaymentLinkStatus.PAID) {
                        if (order.getPaymentStatus() != PaymentStatus.PAID) {
                            order.setPaymentStatus(PaymentStatus.PAID);
                            order.setPaymentDate(Instant.now());
                            orderRepository.save(order);
                        }
                        throw new InvalidRequestException(ErrorCodes.INVALID_REQUEST, "Order already paid via PayOS");
                    }
                    log.info("PayOSService::createPaymentLink - Existing link is {}, creating new one", status);
                }
            } catch (ResourceNotFoundException | InvalidRequestException e) {
                throw e;
            } catch (Exception e) {
                log.info("PayOSService::createPaymentLink - Failed to check existing link: {}", e.getMessage());
            }
        }

        // Create new payment link with timestamp as orderCode
        long payosOrderCode = System.currentTimeMillis();
        long amount = order.getTotal().longValue();

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Don hang " + order.getOrderCode())
                .quantity(1)
                .price(amount)
                .build();

        String returnUrl = frontendUrl + "/payment-success/" + order.getId();
        String cancelUrl = frontendUrl + "/payment-cancel/" + order.getId();

        String description = "DH " + order.getOrderCode();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(payosOrderCode)
                .amount(amount)
                .description(description)
                .item(item)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .buyerName(order.getCustomerName())
                .buyerPhone(order.getCustomerPhone())
                .buyerEmail(order.getCustomerEmail())
                .build();

        try {
            log.info("PayOSService::createPaymentLink - Creating new payment link [payosOrderCode: {}]", payosOrderCode);
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentData);

            // Save payment link info to order
            order.setPayosOrderCode(payosOrderCode);
            order.setPayosPaymentLinkId(response.getPaymentLinkId());
            order.setPayosCheckoutUrl(response.getCheckoutUrl());
            order.setPayosQrCode(response.getQrCode());
            orderRepository.save(order);

            log.info("PayOSService::createPaymentLink - Created and saved [orderId: {}, payosOrderCode: {}]",
                    orderId, payosOrderCode);

            return response;
        } catch (Exception e) {
            log.error("PayOSService::createPaymentLink - Failed to create payment link", e);
            throw new RuntimeException("Failed to create PayOS payment link: " + e.getMessage());
        }
    }

    /**
     * Get payment link info for an order.
     * Returns cached info from Order if available.
     */
    public CreatePaymentLinkResponse getPaymentLink(Long orderId) {
        log.info("PayOSService::getPaymentLink - Start [orderId: {}]", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getPayosOrderCode() == null) {
            throw new ResourceNotFoundException(ErrorCodes.RESOURCE_NOT_FOUND, "No payment link found for this order");
        }

        try {
            PaymentLink link = payOS.paymentRequests().get(order.getPayosOrderCode());

            return CreatePaymentLinkResponse.builder()
                    .orderCode(order.getPayosOrderCode())
                    .paymentLinkId(order.getPayosPaymentLinkId())
                    .checkoutUrl(order.getPayosCheckoutUrl())
                    .qrCode(order.getPayosQrCode())
                    .amount(link.getAmount())
                    .status(link.getStatus())
                    .description("DH " + order.getOrderCode())
                    .currency("VND")
                    .bin("")
                    .accountNumber("")
                    .accountName("")
                    .build();
        } catch (Exception e) {
            log.error("PayOSService::getPaymentLink - Failed to get payment link", e);
            throw new RuntimeException("Failed to get payment link: " + e.getMessage());
        }
    }

    /**
     * Cancel payment link for an order.
     */
    @Transactional
    public void cancelPaymentLink(Long orderId, String reason) {
        log.info("PayOSService::cancelPaymentLink - Start [orderId: {}]", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getPayosOrderCode() == null) {
            log.info("PayOSService::cancelPaymentLink - No payment link to cancel");
            return;
        }

        try {
            payOS.paymentRequests().cancel(order.getPayosOrderCode(), reason != null ? reason : "Order cancelled");
            log.info("PayOSService::cancelPaymentLink - Cancelled [payosOrderCode: {}]", order.getPayosOrderCode());
        } catch (Exception e) {
            log.warn("PayOSService::cancelPaymentLink - Failed to cancel: {}", e.getMessage());
        }

        // Clear payment link info
        order.setPayosOrderCode(null);
        order.setPayosPaymentLinkId(null);
        order.setPayosCheckoutUrl(null);
        order.setPayosQrCode(null);
        orderRepository.save(order);
    }

    /**
     * Handle webhook from PayOS when payment is completed.
     * Finds order by payosOrderCode and updates payment status.
     * Auto-completes POS orders.
     */
    @Transactional
    public void handleWebhook(WebhookData webhookData) {
        Long payosOrderCode = webhookData.getOrderCode();
        log.info("PayOSService::handleWebhook - Start [payosOrderCode: {}]", payosOrderCode);

        // Find order by payosOrderCode
        Order order = orderRepository.findByPayosOrderCode(payosOrderCode)
                .orElseThrow(() -> {
                    log.error("PayOSService::handleWebhook - Order not found [payosOrderCode: {}]", payosOrderCode);
                    return new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND,
                            "Order not found for payosOrderCode: " + payosOrderCode);
                });

        long amountPaid = webhookData.getAmount();
        long orderAmount = order.getTotal().longValue();

        if (amountPaid < orderAmount) {
            log.error("PayOSService::handleWebhook - Payment amount mismatch! Expected: {}, Paid: {}",
                    orderAmount, amountPaid);
            return;
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            log.info("PayOSService::handleWebhook - Order already paid, skipping");
            return;
        }

        // Update payment status
        order.setPaymentStatus(PaymentStatus.PAID);
        order.setPaymentDate(Instant.now());
        order.setTransactionId(webhookData.getReference());

        // Auto complete POS order when payment is received via QR
        if (order.getOrderType() == OrderType.POS_INSTORE && order.getStatus() == OrderStatus.PENDING) {
            log.info("PayOSService::handleWebhook - Auto completing POS order [orderId: {}]", order.getId());

            // Commit reserved stock
            stockService.commitReservedStock(new ArrayList<>(order.getOrderDetails()));

            order.setStatus(OrderStatus.COMPLETED);
            order.setCompletedAt(Instant.now());
        }

        orderRepository.save(order);
        log.info("PayOSService::handleWebhook - Order {} updated to PAID{}",
                order.getOrderCode(),
                order.getStatus() == OrderStatus.COMPLETED ? " and COMPLETED" : "");
    }
}

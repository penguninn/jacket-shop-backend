package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayOSService {

    private final PayOS payOS;
    private final OrderRepository orderRepository;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    public CreatePaymentLinkResponse createPaymentLink(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("This order has been cancelled. Please create a new order.");
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Order already paid");
        }
        
        long amount = order.getTotal().longValue(); 
        long payOsOrderCode = order.getId();

        try {
            PaymentLink existingPayment = payOS.paymentRequests().get(payOsOrderCode);
            if (existingPayment != null) {
            }
        } catch (Exception e) {
            log.info("Creating new payment link as existing one not found or check failed: {}", e.getMessage());
        }

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Don hang " + order.getOrderCode())
                .quantity(1)
                .price(amount)
                .build();

        String returnUrl = frontendUrl + "/payment-success/" + order.getOrderCode(); 
        String cancelUrl = frontendUrl + "/payment-cancel/" + order.getOrderCode();

        String description = "Thanh toan " + order.getOrderCode();
        if (description.length() > 25) {
            description = description.substring(0, 25);
        }

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(payOsOrderCode) 
                .amount(amount)
                .description(description)
                .item(item)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();

        try {
            return payOS.paymentRequests().create(paymentData);
        } catch (Exception e) {
            throw new RuntimeException("Fail to create PayOS link: " + e.getMessage());
        }
    }

    @Transactional
    public void handleWebhook(WebhookData webhookData) {
        Long orderId = webhookData.getOrderCode();
        System.out.println(orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found via Webhook"));

        long amountPaid = webhookData.getAmount();
        long orderAmount = order.getTotal().longValue();

        if (amountPaid < orderAmount) {
            log.error("Payment amount mismatch! Expected: {}, Paid: {}", orderAmount, amountPaid);
            return;
        }

        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentDate(Instant.now());
            
            
            orderRepository.save(order);
            log.info("Order {} updated to PAID via Webhook", order.getOrderCode());
        }
    }
}

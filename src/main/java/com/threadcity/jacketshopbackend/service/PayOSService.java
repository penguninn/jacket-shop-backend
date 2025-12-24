package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.webhooks.WebhookData;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayOSService {

    private final PayOS payOS;
    private final OrderRepository orderRepository;

    public CreatePaymentLinkResponse createPaymentLink(Long orderId) {
        // 1. Lấy thông tin đơn hàng thật từ Database
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));

        // 2. Kiểm tra xem đơn đã thanh toán chưa
        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new RuntimeException("Order already paid");
        }
        
        // 3. Tính toán tiền (Chuyển BigDecimal sang long)
        long amount = order.getTotal().longValue(); 
        
        // Use ID as PayOS orderCode (must be numeric and unique enough in PayOS context)
        long payOsOrderCode = order.getId();

        PaymentLinkItem item = PaymentLinkItem.builder()
                .name("Đơn hàng " + order.getOrderCode())
                .quantity(1)
                .price(amount)
                .build();

        // 4. Build Request
        // In real app, these URLs should be configured or constructed dynamically
        String returnUrl = "http://localhost:3000/payment-success/" + order.getOrderCode(); 
        String cancelUrl = "http://localhost:3000/payment-cancel/" + order.getOrderCode();

        CreatePaymentLinkRequest paymentData = CreatePaymentLinkRequest.builder()
                .orderCode(payOsOrderCode) 
                .amount(amount)
                .description("Thanh toan don " + order.getOrderCode())
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
        // 1. Lấy orderCode (chính là ID của Order) từ webhook
        Long orderId = webhookData.getOrderCode();

        // 2. Tìm đơn hàng trong DB
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found via Webhook"));

        // 3. Check an toàn: Số tiền chuyển có khớp với đơn hàng không?
        long amountPaid = webhookData.getAmount();
        long orderAmount = order.getTotal().longValue();

        if (amountPaid < orderAmount) {
            log.error("Payment amount mismatch! Expected: {}, Paid: {}", orderAmount, amountPaid);
            // Could mark as PARTIAL or notify admin
            return;
        }

        // 4. Cập nhật trạng thái đơn hàng
        if (order.getPaymentStatus() != PaymentStatus.PAID) {
            order.setPaymentStatus(PaymentStatus.PAID);
            order.setPaymentDate(Instant.now());
            
            // Optional: Save transaction ref if available
            // order.setTransactionId(webhookData.getReference()); 
            
            orderRepository.save(order);
            log.info("Order {} updated to PAID via Webhook", order.getOrderCode());
        }
    }
}

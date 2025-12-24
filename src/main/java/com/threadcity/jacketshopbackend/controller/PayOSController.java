package com.threadcity.jacketshopbackend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.threadcity.jacketshopbackend.dto.request.payos.ConfirmWebhookRequest;
import com.threadcity.jacketshopbackend.dto.request.payos.CreatePaymentLinkRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.core.FileDownloadResponse;
import vn.payos.exception.APIException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLink;
import vn.payos.model.v2.paymentRequests.PaymentLinkItem;
import vn.payos.model.v2.paymentRequests.invoices.InvoicesInfo;
import vn.payos.model.webhooks.ConfirmWebhookResponse;
import vn.payos.model.webhooks.WebhookData;

import java.time.Instant;
import java.util.Map;

import com.threadcity.jacketshopbackend.service.PayOSService;

@RestController
@RequestMapping("/api/payos")
@RequiredArgsConstructor
@Slf4j
public class PayOSController {

    private final PayOS payOS;
    private final PayOSService payOSService;

    @PostMapping("/payment-link/{orderId}")
    public ApiResponse<?> createPaymentLink(@PathVariable Long orderId) {
        log.info("PayOSController::createPaymentLink - Start [orderId: {}]", orderId);
        try {
            CreatePaymentLinkResponse data = payOSService.createPaymentLink(orderId);
            log.info("PayOSController::createPaymentLink - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Payment link created successfully")
                    .data(data)
                    .timestamp(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("PayOSController::createPaymentLink - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @GetMapping("/orders/{orderId}")
    public ApiResponse<?> getOrderById(@PathVariable long orderId) {
        log.info("PayOSController::getOrderById - Start [orderId: {}]", orderId);
        try {
            PaymentLink order = payOS.paymentRequests().get(orderId);
            log.info("PayOSController::getOrderById - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Get order successfully")
                    .data(order)
                    .timestamp(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("PayOSController::getOrderById - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @PutMapping("/orders/{orderId}/cancel")
    public ApiResponse<?> cancelOrder(@PathVariable long orderId) {
        log.info("PayOSController::cancelOrder - Start [orderId: {}]", orderId);
        try {
            PaymentLink order = payOS.paymentRequests().cancel(orderId, "Cancelled by user");
            log.info("PayOSController::cancelOrder - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Order cancelled successfully")
                    .data(order)
                    .timestamp(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("PayOSController::cancelOrder - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @PostMapping("/confirm-webhook")
    public ApiResponse<?> confirmWebhook(@RequestBody ConfirmWebhookRequest request) {
        log.info("PayOSController::confirmWebhook - Start");
        try {
            ConfirmWebhookResponse result = payOS.webhooks().confirm(request.getWebhookUrl());
            log.info("PayOSController::confirmWebhook - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Webhook confirmed successfully")
                    .data(result)
                    .timestamp(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("PayOSController::confirmWebhook - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @GetMapping("/orders/{orderId}/invoices")
    public ApiResponse<?> retrieveInvoices(@PathVariable long orderId) {
        log.info("PayOSController::retrieveInvoices - Start [orderId: {}]", orderId);
        try {
            InvoicesInfo invoicesInfo = payOS.paymentRequests().invoices().get(orderId);
            log.info("PayOSController::retrieveInvoices - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Get invoices successfully")
                    .data(invoicesInfo)
                    .timestamp(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("PayOSController::retrieveInvoices - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @GetMapping("/orders/{orderId}/invoices/{invoiceId}/download")
    public ResponseEntity<?> downloadInvoice(@PathVariable long orderId, @PathVariable String invoiceId) {
        log.info("PayOSController::downloadInvoice - Start [orderId: {}, invoiceId: {}]", orderId, invoiceId);
        try {
            FileDownloadResponse invoiceFile = payOS.paymentRequests().invoices().download(invoiceId, orderId);

            if (invoiceFile == null || invoiceFile.getData() == null) {
                return ResponseEntity.status(404)
                        .body(ApiResponse.builder()
                                .code(404)
                                .message("Invoice not found or empty")
                                .timestamp(Instant.now())
                                .build());
            }

            ByteArrayResource resource = new ByteArrayResource(invoiceFile.getData());

            HttpHeaders headers = new HttpHeaders();
            String contentType = invoiceFile.getContentType() == null
                    ? MediaType.APPLICATION_PDF_VALUE
                    : invoiceFile.getContentType();
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + invoiceFile.getFilename() + "\"");
            if (invoiceFile.getSize() != null) {
                headers.setContentLength(invoiceFile.getSize());
            }

            log.info("PayOSController::downloadInvoice - Success");
            return ResponseEntity.ok().headers(headers).body(resource);

        } catch (APIException e) {
            log.error("PayOSController::downloadInvoice - API Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.builder()
                            .code(500)
                            .message(e.getErrorDesc().orElse(e.getMessage()))
                            .timestamp(Instant.now())
                            .build());
        } catch (Exception e) {
            log.error("PayOSController::downloadInvoice - Error: {}", e.getMessage(), e);
            return ResponseEntity.status(500)
                    .body(ApiResponse.builder()
                            .code(500)
                            .message(e.getMessage())
                            .timestamp(Instant.now())
                            .build());
        }
    }
    
    @PostMapping("/transfer-handler")
    public ApiResponse<?> payosTransferHandler(@RequestBody Object body) {
        log.info("PayOSController::payosTransferHandler - Start");
        try {
            WebhookData data = payOS.webhooks().verify(body);
            payOSService.handleWebhook(data);
            log.info("PayOSController::payosTransferHandler - Success: {}", data);
            return ApiResponse.builder()
                    .code(200)
                    .message("Webhook processed")
                    .data(data)
                    .timestamp(Instant.now())
                    .build();
        } catch (Exception e) {
            log.error("PayOSController::payosTransferHandler - Error: {}", e.getMessage(), e);
             return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }
}

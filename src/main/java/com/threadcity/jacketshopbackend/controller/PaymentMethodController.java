package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.common.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.request.common.BulkStatusRequest;
import com.threadcity.jacketshopbackend.dto.request.PaymentMethodRequest;
import com.threadcity.jacketshopbackend.dto.request.common.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.filter.PaymentMethodFilterRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.PaymentMethodResponse;
import com.threadcity.jacketshopbackend.service.PaymentMethodService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/payment-methods")
@RequiredArgsConstructor
@Slf4j
public class PaymentMethodController {

        private final PaymentMethodService paymentMethodService;

        @GetMapping
        public ApiResponse<?> getAllPaymentMethods(
                        @RequestParam(required = false) String search,
                        @RequestParam(required = false) List<String> status,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        @RequestParam(defaultValue = "createdAt") String sortBy,
                        @RequestParam(defaultValue = "DESC") String sortDir) {
                log.info("PaymentMethodController::getAllPaymentMethods - Execution started");

                PaymentMethodFilterRequest request = PaymentMethodFilterRequest.builder()
                                .search(search)
                                .status(status)
                                .page(page)
                                .size(size)
                                .sortBy(sortBy)
                                .sortDir(sortDir)
                                .build();

                PageResponse<?> pageResponse = paymentMethodService.getAllPaymentMethods(request);

                log.info("PaymentMethodController::getAllPaymentMethods - Execution completed");

                return ApiResponse.builder()
                                .code(200)
                                .message("Get all payment methods successfully.")
                                .data(pageResponse)
                                .timestamp(Instant.now())
                                .build();
        }

        @GetMapping("/{id}")
        public ApiResponse<?> getPaymentMethodById(@PathVariable Long id) {
                log.info("PaymentMethodController::getPaymentMethodById - Execution started. [id: {}]", id);

                PaymentMethodResponse response = paymentMethodService.getPaymentMethodById(id);

                log.info("PaymentMethodController::getPaymentMethodById - Execution completed. [id: {}]", id);

                return ApiResponse.builder()
                                .code(200)
                                .message("Get payment method by ID successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping
        public ApiResponse<?> createPaymentMethod(@Valid @RequestBody PaymentMethodRequest request) {
                log.info("PaymentMethodController::createPaymentMethod - Execution started.");

                PaymentMethodResponse response = paymentMethodService.createPaymentMethod(request);

                log.info("PaymentMethodController::createPaymentMethod - Execution completed.");

                return ApiResponse.builder()
                                .code(201)
                                .message("Payment method created successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}")
        public ApiResponse<?> updatePaymentMethod(
                        @PathVariable Long id,
                        @Valid @RequestBody PaymentMethodRequest request) {
                log.info("PaymentMethodController::updatePaymentMethod - Execution started. [id: {}]", id);

                PaymentMethodResponse response = paymentMethodService.updatePaymentMethodById(request, id);

                log.info("PaymentMethodController::updatePaymentMethod - Execution completed. [id: {}]", id);

                return ApiResponse.builder()
                                .code(200)
                                .message("Payment method updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @DeleteMapping("/{id}")
        public ApiResponse<?> deletePaymentMethod(@PathVariable Long id) {
                log.info("PaymentMethodController::deletePaymentMethod - Execution started. [id: {}]", id);

                paymentMethodService.deletePaymentMethod(id);

                log.info("PaymentMethodController::deletePaymentMethod - Execution completed. [id: {}]", id);

                return ApiResponse.builder()
                                .code(200)
                                .message("Payment method deleted successfully.")
                                .timestamp(Instant.now())
                                .build();
        }

        @PutMapping("/{id}/status")
        public ApiResponse<?> updateStatus(
                        @PathVariable Long id,
                        @Valid @RequestBody UpdateStatusRequest request) {
                log.info("PaymentMethodController::updateStatus - Execution started. [id: {}]", id);
                PaymentMethodResponse response = paymentMethodService.updateStatus(request, id);
                log.info("PaymentMethodController::updateStatus - Execution completed. [id: {}]", id);

                return ApiResponse.builder()
                                .code(200)
                                .message("Payment method status updated successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/status")
        public ApiResponse<?> bulkUpdateStatus(@Valid @RequestBody BulkStatusRequest request) {
                log.info("PaymentMethodController::bulkUpdateStatus - Execution started.");
                List<PaymentMethodResponse> response = paymentMethodService.bulkUpdatePaymentMethodsStatus(request);
                log.info("PaymentMethodController::bulkUpdateStatus - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk update payment methods status successfully.")
                                .data(response)
                                .timestamp(Instant.now())
                                .build();
        }

        @PostMapping("/bulk/delete")
        public ApiResponse<?> bulkDelete(@Valid @RequestBody BulkDeleteRequest request) {
                log.info("PaymentMethodController::bulkDelete - Execution started.");
                paymentMethodService.bulkDeletePaymentMethods(request);
                log.info("PaymentMethodController::bulkDelete - Execution completed.");

                return ApiResponse.builder()
                                .code(200)
                                .message("Bulk delete payment methods successfully.")
                                .timestamp(Instant.now())
                                .build();
        }
}

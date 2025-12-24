package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.dto.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.request.ShippingInfoRequest;
import com.threadcity.jacketshopbackend.dto.request.UpdatePaymentRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.service.OrderService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;

    // ==================== ORDER MANAGEMENT ====================

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> getAllOrders(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) OrderType orderType,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) PaymentStatus paymentStatus,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        log.info("AdminOrderController::getAllOrders - Execution started.");
        OrderFilterRequest request = new OrderFilterRequest();
        request.setOrderCode(orderCode);
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setOrderType(orderType);
        request.setStatus(status);
        request.setPaymentStatus(paymentStatus);
        request.setUserId(userId);
        request.setStaffId(staffId);
        request.setPage(page);
        request.setSize(size);
        request.setSortBy(sortBy);
        request.setSortDir(sortDir);

        PageResponse<?> response = orderService.getAllOrders(request);
        log.info("AdminOrderController::getAllOrders - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get all orders successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> getOrderById(@PathVariable Long id) {
        log.info("AdminOrderController::getOrderById - Execution started. [id: {}]", id);
        var response = orderService.getOrderById(id);
        log.info("AdminOrderController::getOrderById - Execution completed. [id: {}]", id);
        return ApiResponse.builder()
                .code(200)
                .message("Get order by ID successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/payment-status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> updatePaymentStatus(@PathVariable Long id, @RequestBody UpdatePaymentRequest request) {
        log.info("AdminOrderController::updatePaymentStatus - Execution started. [id: {}]", id);
        var response = orderService.updatePaymentStatus(id, request);
        log.info("AdminOrderController::updatePaymentStatus - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Payment status updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/shipping-info")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> updateShippingInfo(@PathVariable Long id, @RequestBody ShippingInfoRequest request) {
        log.info("AdminOrderController::updateShippingInfo - Execution started. [id: {}]", id);
        var response = orderService.updateShippingInfo(id, request);
        log.info("AdminOrderController::updateShippingInfo - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Shipping info updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> confirmOrder(@PathVariable Long id) {
        log.info("AdminOrderController::confirmOrder - Execution started. [id: {}]", id);
        var response = orderService.confirmOrder(id);
        log.info("AdminOrderController::confirmOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order confirmed successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> shipOrder(@PathVariable Long id) {
        log.info("AdminOrderController::shipOrder - Execution started. [id: {}]", id);
        var response = orderService.shipOrder(id);
        log.info("AdminOrderController::shipOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order shipped successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> completeOrder(@PathVariable Long id) {
        log.info("AdminOrderController::completeOrder - Execution started. [id: {}]", id);
        var response = orderService.completeOrder(id);
        log.info("AdminOrderController::completeOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order completed successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<?> cancelOrder(@PathVariable Long id) {
        log.info("AdminOrderController::cancelOrder - Execution started. [id: {}]", id);
        var response = orderService.cancelOrder(id);
        log.info("AdminOrderController::cancelOrder - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Order cancelled successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/{id}/return/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<OrderResponse> approveReturn(@PathVariable Long id) {
        log.info("AdminOrderController::approveReturn - Execution started. [id: {}]", id);
        OrderResponse response = orderService.approveReturn(id);
        log.info("AdminOrderController::approveReturn - Execution completed.");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Return approved successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== POS OPERATIONS ====================

    @PostMapping("/pos/draft")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<OrderResponse> createPosDraft(@RequestBody OrderRequest request) {
        log.info("AdminOrderController::createPosDraft - Execution started.");
        OrderResponse response = orderService.createPosDraft(request);
        log.info("AdminOrderController::createPosDraft - Execution completed.");
        return ApiResponse.<OrderResponse>builder()
                .code(201)
                .message("POS draft created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/pos/drafts")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<List<OrderResponse>> getPosDrafts() {
        log.info("AdminOrderController::getPosDrafts - Execution started.");
        List<OrderResponse> response = orderService.getPosDrafts();
        log.info("AdminOrderController::getPosDrafts - Execution completed.");
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .message("Get POS drafts successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{id}/complete")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<OrderResponse> completePosOrder(@PathVariable Long id) {
        log.info("AdminOrderController::completePosOrder - Execution started. [id: {}]", id);
        OrderResponse response = orderService.completePosOrder(id);
        log.info("AdminOrderController::completePosOrder - Execution completed.");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("POS order completed successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<OrderResponse> updatePosDraft(
            @PathVariable Long id,
            @RequestBody OrderRequest request) {
        log.info("AdminOrderController::updatePosDraft - Execution started. [id: {}]", id);
        OrderResponse response = orderService.updatePosDraft(id, request);
        log.info("AdminOrderController::updatePosDraft - Execution completed.");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("POS draft updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/pos/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    public ApiResponse<Void> cancelPosDraft(@PathVariable Long id) {
        log.info("AdminOrderController::cancelPosDraft - Execution started. [id: {}]", id);
        orderService.cancelPosDraft(id);
        log.info("AdminOrderController::cancelPosDraft - Execution completed.");
        return ApiResponse.<Void>builder()
                .code(200)
                .message("POS draft cancelled successfully.")
                .data(null)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== POS DRAFT ITEM MANAGEMENT ====================

    @PostMapping("/pos/{draftId}/items")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ApiResponse<OrderResponse> addItemToPosDraft(
            @PathVariable Long draftId,
            @RequestBody OrderItemRequest item) {
        log.info("AdminOrderController::addItemToPosDraft - Execution started. [draftId: {}, variantId: {}]",
                draftId, item.getProductVariantId());
        OrderResponse response = orderService.addItemToPosDraft(draftId, item);
        log.info("AdminOrderController::addItemToPosDraft - Execution completed.");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Item added to POS draft successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{draftId}/items/{itemId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ApiResponse<OrderResponse> updateDraftItemQuantity(
            @PathVariable Long draftId,
            @PathVariable Long itemId,
            @RequestParam @Min(0) Integer quantity) {
        log.info("AdminOrderController::updateDraftItemQuantity - Execution started. [draftId: {}, itemId: {}, quantity: {}]",
                draftId, itemId, quantity);
        OrderResponse response = orderService.updateDraftItemQuantity(draftId, itemId, quantity);
        log.info("AdminOrderController::updateDraftItemQuantity - Execution completed.");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Draft item quantity updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/pos/{draftId}/items/{itemId}")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ApiResponse<OrderResponse> removeItemFromPosDraft(
            @PathVariable Long draftId,
            @PathVariable Long itemId) {
        log.info("AdminOrderController::removeItemFromPosDraft - Execution started. [draftId: {}, itemId: {}]",
                draftId, itemId);
        OrderResponse response = orderService.removeItemFromDraft(draftId, itemId);
        log.info("AdminOrderController::removeItemFromPosDraft - Execution completed.");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Item removed from POS draft successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
}
package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.common.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.order.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.order.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.order.request.ShippingInfoRequest;
import com.threadcity.jacketshopbackend.dto.order.request.UpdatePaymentRequest;
import com.threadcity.jacketshopbackend.dto.order.response.OrderHistoryResponse;
import com.threadcity.jacketshopbackend.dto.order.response.OrderResponse;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    // ==================== READ OPERATIONS ====================

    @GetMapping("/{id}")
    public ApiResponse<OrderResponse> getOrderById(@PathVariable Long id) {
        log.info("OrderController::getOrderById - Start [id: {}]", id);
        OrderResponse response = orderService.getOrderById(id);
        log.info("OrderController::getOrderById - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order retrieved successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<PageResponse<?>> getAllOrders(
            @RequestParam(required = false) String orderCode,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Enums.OrderStatus status,
            @RequestParam(required = false) String orderType,
            @RequestParam(required = false) String paymentStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        log.info("OrderController::getAllOrders - Start");

        OrderFilterRequest request = OrderFilterRequest.builder()
                .orderCode(orderCode)
                .userId(userId)
                .staffId(staffId)
                .status(status)
                .orderType(orderType != null ? Enums.OrderType.valueOf(orderType) : null)
                .paymentStatus(paymentStatus != null ? Enums.PaymentStatus.valueOf(paymentStatus) : null)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<?> response = orderService.getAllOrders(request);

        log.info("OrderController::getAllOrders - Completed");
        return ApiResponse.<PageResponse<?>>builder()
                .code(200)
                .message("Orders retrieved successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/my-orders")
    public ApiResponse<List<OrderResponse>> getMyOrders(
            @RequestParam(required = false) Enums.OrderStatus status) {
        log.info("OrderController::getMyOrders - Start [status: {}]", status);
        List<OrderResponse> response = orderService.getMyOrders(status);
        log.info("OrderController::getMyOrders - Completed [count: {}]", response.size());
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .message("Orders retrieved successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/{id}/history")
    public ApiResponse<List<OrderHistoryResponse>> getOrderHistory(@PathVariable Long id) {
        log.info("OrderController::getOrderHistory - Start [id: {}]", id);
        List<OrderHistoryResponse> response = orderService.getOrderHistory(id);
        log.info("OrderController::getOrderHistory - Completed");
        return ApiResponse.<List<OrderHistoryResponse>>builder()
                .code(200)
                .message("Order history retrieved successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== CREATE OPERATIONS ====================

    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        log.info("OrderController::createOrder - Start [type: {}]", request.getOrderType());
        OrderResponse response = orderService.createOrder(request);
        log.info("OrderController::createOrder - Completed [code: {}]", response.getOrderCode());
        return ApiResponse.<OrderResponse>builder()
                .code(201)
                .message("Order created successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/{id}/reorder")
    public ApiResponse<Void> reorder(@PathVariable Long id) {
        log.info("OrderController::reorder - Start [id: {}]", id);
        orderService.reorder(id);
        log.info("OrderController::reorder - Completed");
        return ApiResponse.<Void>builder()
                .code(200)
                .message("Items added to cart successfully")
                .timestamp(Instant.now())
                .build();
    }

    // ==================== STATE TRANSITIONS ====================

    @PutMapping("/{id}/confirm")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> confirmOrder(@PathVariable Long id) {
        log.info("OrderController::confirmOrder - Start [id: {}]", id);
        OrderResponse response = orderService.confirmOrder(id);
        log.info("OrderController::confirmOrder - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order confirmed successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> shipOrder(@PathVariable Long id) {
        log.info("OrderController::shipOrder - Start [id: {}]", id);
        OrderResponse response = orderService.shipOrder(id);
        log.info("OrderController::shipOrder - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order shipped successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> completeOrder(@PathVariable Long id) {
        log.info("OrderController::completeOrder - Start [id: {}]", id);
        OrderResponse response = orderService.completeOrder(id);
        log.info("OrderController::completeOrder - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order completed successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/receive")
    public ApiResponse<OrderResponse> receiveOrder(@PathVariable Long id) {
        log.info("OrderController::receiveOrder - Start [id: {}]", id);
        OrderResponse response = orderService.receiveOrder(id);
        log.info("OrderController::receiveOrder - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order received successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/cancel")
    public ApiResponse<OrderResponse> cancelOrder(@PathVariable Long id) {
        log.info("OrderController::cancelOrder - Start [id: {}]", id);
        OrderResponse response = orderService.cancelOrder(id);
        log.info("OrderController::cancelOrder - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Order cancelled successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== RETURN OPERATIONS ====================

    @PostMapping("/{id}/return")
    public ApiResponse<OrderResponse> requestReturn(
            @PathVariable Long id,
            @RequestParam String reason) {
        log.info("OrderController::requestReturn - Start [id: {}]", id);
        OrderResponse response = orderService.requestReturn(id, reason);
        log.info("OrderController::requestReturn - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Return requested successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/return/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> approveReturn(@PathVariable Long id) {
        log.info("OrderController::approveReturn - Start [id: {}]", id);
        OrderResponse response = orderService.approveReturn(id);
        log.info("OrderController::approveReturn - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Return approved successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== UPDATE OPERATIONS ====================

    @PutMapping("/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentRequest request) {
        log.info("OrderController::updatePaymentStatus - Start [id: {}]", id);
        OrderResponse response = orderService.updatePaymentStatus(id, request);
        log.info("OrderController::updatePaymentStatus - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Payment status updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/{id}/shipping")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updateShippingInfo(
            @PathVariable Long id,
            @Valid @RequestBody ShippingInfoRequest request) {
        log.info("OrderController::updateShippingInfo - Start [id: {}]", id);
        OrderResponse response = orderService.updateShippingInfo(id, request);
        log.info("OrderController::updateShippingInfo - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Shipping info updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== POS OPERATIONS ====================

    @GetMapping("/pos/drafts")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<List<OrderResponse>> getPosDrafts() {
        log.info("OrderController::getPosDrafts - Start");
        List<OrderResponse> response = orderService.getPosDrafts();
        log.info("OrderController::getPosDrafts - Completed [count: {}]", response.size());
        return ApiResponse.<List<OrderResponse>>builder()
                .code(200)
                .message("POS drafts retrieved successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PostMapping("/pos/draft")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> createPosDraft(@Valid @RequestBody OrderRequest request) {
        log.info("OrderController::createPosDraft - Start");
        OrderResponse response = orderService.createPosDraft(request);
        log.info("OrderController::createPosDraft - Completed [code: {}]", response.getOrderCode());
        return ApiResponse.<OrderResponse>builder()
                .code(201)
                .message("POS draft created successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> completePosOrder(@PathVariable Long id) {
        log.info("OrderController::completePosOrder - Start [id: {}]", id);
        OrderResponse response = orderService.completePosOrder(id);
        log.info("OrderController::completePosOrder - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("POS order completed successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/pos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<Void> cancelPosDraft(@PathVariable Long id) {
        log.info("OrderController::cancelPosDraft - Start [id: {}]", id);
        orderService.cancelPosDraft(id);
        log.info("OrderController::cancelPosDraft - Completed");
        return ApiResponse.<Void>builder()
                .code(200)
                .message("POS draft cancelled successfully")
                .timestamp(Instant.now())
                .build();
    }

    // ==================== POS ITEM MANAGEMENT ====================

    @PostMapping("/pos/{draftId}/items")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> addItemToPosDraft(
            @PathVariable Long draftId,
            @Valid @RequestBody OrderItemRequest item) {
        log.info("OrderController::addItemToPosDraft - Start [draftId: {}]", draftId);
        OrderResponse response = orderService.addItemToPosDraft(draftId, item);
        log.info("OrderController::addItemToPosDraft - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Item added to draft successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{draftId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updateDraftItemQuantity(
            @PathVariable Long draftId,
            @PathVariable Long itemId,
            @RequestParam Integer quantity) {
        log.info("OrderController::updateDraftItemQuantity - Start [draftId: {}, itemId: {}]", draftId, itemId);
        OrderResponse response = orderService.updateDraftItemQuantity(draftId, itemId, quantity);
        log.info("OrderController::updateDraftItemQuantity - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Item quantity updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/pos/{draftId}/items/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> removeItemFromDraft(
            @PathVariable Long draftId,
            @PathVariable Long itemId) {
        log.info("OrderController::removeItemFromDraft - Start [draftId: {}, itemId: {}]", draftId, itemId);
        OrderResponse response = orderService.removeItemFromDraft(draftId, itemId);
        log.info("OrderController::removeItemFromDraft - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Item removed from draft successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ==================== POS DRAFT UPDATES ====================

    @PutMapping("/pos/{id}/info")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updatePosDraftInfo(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {
        log.info("OrderController::updatePosDraftInfo - Start [id: {}]", id);
        OrderResponse response = orderService.updatePosDraftInfo(id, request);
        log.info("OrderController::updatePosDraftInfo - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Draft info updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{id}/customer")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updatePosDraftCustomer(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {
        log.info("OrderController::updatePosDraftCustomer - Start [id: {}]", id);
        OrderResponse response = orderService.updatePosDraftCustomer(id, request);
        log.info("OrderController::updatePosDraftCustomer - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Customer info updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{id}/shipping")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updatePosDraftShipping(
            @PathVariable Long id,
            @Valid @RequestBody OrderRequest request) {
        log.info("OrderController::updatePosDraftShipping - Start [id: {}]", id);
        OrderResponse response = orderService.updatePosDraftShipping(id, request);
        log.info("OrderController::updatePosDraftShipping - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Shipping info updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{id}/coupon")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updatePosDraftCoupon(
            @PathVariable Long id,
            @RequestParam(required = false) String couponCode) {
        log.info("OrderController::updatePosDraftCoupon - Start [id: {}]", id);
        OrderResponse response = orderService.updatePosDraftCoupon(id, couponCode);
        log.info("OrderController::updatePosDraftCoupon - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Coupon updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @PutMapping("/pos/{id}/payment")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ApiResponse<OrderResponse> updatePosPayment(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePaymentRequest request) {
        log.info("OrderController::updatePosPayment - Start [id: {}]", id);
        OrderResponse response = orderService.updatePosPayment(id, request);
        log.info("OrderController::updatePosPayment - Completed");
        return ApiResponse.<OrderResponse>builder()
                .code(200)
                .message("Payment info updated successfully")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
}

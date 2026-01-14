package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.order.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.order.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.order.request.ShippingInfoRequest;
import com.threadcity.jacketshopbackend.dto.order.request.UpdatePaymentRequest;
import com.threadcity.jacketshopbackend.dto.order.response.OrderHistoryResponse;
import com.threadcity.jacketshopbackend.dto.order.response.OrderResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OnlineOrderService onlineOrderService;
    private final PosOrderService posOrderService;
    private final OrderRepository orderRepository;

    public OrderResponse getOrderById(Long id) {
        return onlineOrderService.getOrderById(id);
    }

    public PageResponse<?> getAllOrders(OrderFilterRequest request) {
        return onlineOrderService.getAllOrders(request);
    }

    public List<OrderResponse> getMyOrders(OrderStatus status) {
        return onlineOrderService.getMyOrders(status);
    }

    public List<OrderHistoryResponse> getOrderHistory(Long orderId) {
        return onlineOrderService.getOrderHistory(orderId);
    }

    @SuppressWarnings("unchecked")
    public List<OrderResponse> getPosDrafts() {
        OrderFilterRequest filter = new OrderFilterRequest();
        filter.setOrderType(OrderType.POS_INSTORE);
        filter.setStatus(OrderStatus.PENDING);
        filter.setStaffId(getUserId());
        filter.setSortBy("createdAt");
        filter.setSortDir("desc");

        PageResponse<?> page = onlineOrderService.getAllOrders(filter);
        return (List<OrderResponse>) page.getContents();
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        return onlineOrderService.createOrder(request);
    }

    @Transactional
    public OrderResponse createPosDraft() {
        return posOrderService.createPosDraft();
    }

    // ==================== STATE TRANSITIONS ====================

    @Transactional
    public OrderResponse confirmOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.confirmOrder(id);
        } else {
            throw new InvalidRequestException(ErrorCodes.INVALID_REQUEST,
                    "POS orders cannot be confirmed via this endpoint");
        }
    }

    @Transactional
    public OrderResponse shipOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.shipOrder(id);
        } else {
            throw new InvalidRequestException(ErrorCodes.INVALID_REQUEST, "POS orders cannot be shipped via this endpoint");
        }
    }

    @Transactional
    public OrderResponse completeOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.completeOrder(id);
        } else {
            return posOrderService.completePosOrder(id);
        }
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.cancelOrder(id);
        } else {
            posOrderService.cancelPosDraft(id);
            return null;
        }
    }

    @Transactional
    public OrderResponse receiveOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.receiveOrder(id);
        } else {
            return posOrderService.completePosOrder(id);
        }
    }

    // ==================== RETURN OPERATIONS ====================

    @Transactional
    public OrderResponse requestReturn(Long id, String reason) {
        return onlineOrderService.requestReturn(id, reason);
    }

    @Transactional
    public OrderResponse approveReturn(Long id) {
        return onlineOrderService.approveReturn(id);
    }

    // ==================== UPDATE OPERATIONS ====================

    @Transactional
    public OrderResponse updateShippingInfo(Long id, ShippingInfoRequest request) {
        return onlineOrderService.updateShippingInfo(id, request);
    }

    @Transactional
    public void reorder(Long id) {
        onlineOrderService.reorder(id);
    }

    // ==================== POS SPECIFIC OPERATIONS ====================

    @Transactional
    public OrderResponse completePosOrder(Long id) {
        return posOrderService.completePosOrder(id);
    }

    @Transactional
    public void cancelPosDraft(Long id) {
        posOrderService.cancelPosDraft(id);
    }

    @Transactional
    public OrderResponse addItemToPosDraft(Long draftId, OrderItemRequest item) {
        log.info("OrderService::addItemToPosDraft - Start [draftId: {}]", draftId);
        validatePosDraft(draftId);
        return posOrderService.addItemToDraft(draftId, item);
    }

    @Transactional
    public OrderResponse updateDraftItemQuantity(Long draftId, Long itemId, Integer quantity) {
        log.info("OrderService::updateDraftItemQuantity - Start [draftId: {}, itemId: {}]", draftId, itemId);
        validatePosDraft(draftId);
        return posOrderService.updateDraftItemQuantity(draftId, itemId, quantity);
    }

    @Transactional
    public OrderResponse removeItemFromDraft(Long draftId, Long itemId) {
        log.info("OrderService::removeItemFromDraft - Start [draftId: {}, itemId: {}]", draftId, itemId);
        validatePosDraft(draftId);
        return posOrderService.removeItemFromDraft(draftId, itemId);
    }

    @Transactional
    public OrderResponse updatePosDraftInfo(Long id, OrderRequest request) {
        return posOrderService.updatePosDraftInfo(id, request);
    }

    @Transactional
    public OrderResponse updatePosDraftCustomer(Long draftId, Long customerId) {
        return posOrderService.updatePosDraftCustomer(draftId, customerId);
    }

    @Transactional
    public OrderResponse updatePosDraftCoupon(Long id, String couponCode) {
        return posOrderService.updatePosDraftCoupon(id, couponCode);
    }

    @Transactional
    public OrderResponse updatePosPayment(Long id, UpdatePaymentRequest request) {
        return posOrderService.updatePosPayment(id, request);
    }

    // ==================== HELPER METHODS ====================

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));
    }

    private void validatePosDraft(Long draftId) {
        Order order = findOrder(draftId);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidRequestException(ErrorCodes.INVALID_ORDER_STATUS,
                    "Order must be PENDING draft to modify items");
        }

        if (order.getOrderType() == OrderType.ONLINE) {
            throw new InvalidRequestException(ErrorCodes.INVALID_REQUEST,
                    "Cannot modify ONLINE orders via this endpoint");
        }

        Long currentStaffId = getUserId();
        if (order.getStaff() == null || !order.getStaff().getId().equals(currentStaffId)) {
            throw new AccessDeniedException("You can only modify your own drafts");
        }
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}

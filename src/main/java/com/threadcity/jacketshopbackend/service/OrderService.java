package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import com.threadcity.jacketshopbackend.dto.request.OrderItemRequest;
import com.threadcity.jacketshopbackend.dto.request.OrderRequest;
import com.threadcity.jacketshopbackend.dto.request.ShippingInfoRequest;
import com.threadcity.jacketshopbackend.dto.request.UpdatePaymentRequest;
import com.threadcity.jacketshopbackend.dto.response.OrderHistoryResponse;
import com.threadcity.jacketshopbackend.dto.response.OrderResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.InvalidRequestException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.OrderFilterRequest;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OnlineOrderService onlineOrderService;
    private final PosOrderService posOrderService;
    private final OrderRepository orderRepository;

    public List<OrderHistoryResponse> getOrderHistory(Long orderId) {
        return onlineOrderService.getOrderHistory(orderId);
    }

    public PageResponse<?> getAllOrders(OrderFilterRequest request) {
        return onlineOrderService.getAllOrders(request);
    }

    public List<OrderResponse> getMyOrders(OrderStatus status) {
        return onlineOrderService.getMyOrders(status);
    }

    public List<OrderResponse> getMyOrders() {
        return onlineOrderService.getMyOrders();
    }

    public OrderResponse getOrderById(Long id) {
        return onlineOrderService.getOrderById(id);
    }

    // --- Write Operations (Routed by Type) ---

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.createOrder(request);
        } else {
            return posOrderService.createOrder(request);
        }
    }

    @Transactional
    public void reorder(Long id) {
        onlineOrderService.reorder(id);
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

    @Transactional
    public OrderResponse requestReturn(Long id, String reason) {
        return onlineOrderService.requestReturn(id, reason);
    }
    
    @Transactional
    public OrderResponse approveReturn(Long id) {
        return onlineOrderService.approveReturn(id);
    }

    @Transactional
    public OrderResponse updatePaymentStatus(Long id, UpdatePaymentRequest request) {
        return onlineOrderService.updatePaymentStatus(id, request);
    }
    
    // --- POS Specific ---

    @Transactional
    public OrderResponse createPosDraft(OrderRequest request) {
        return posOrderService.createPosDraft(request);
    }

    @Transactional
    public OrderResponse completePosOrder(Long id) {
        return posOrderService.completePosOrder(id);
    }

    @Deprecated
    @Transactional
    public OrderResponse updatePosDraft(Long id, OrderRequest request) {
        return posOrderService.updatePosDraft(id, request);
    }
    
    @Transactional
    public OrderResponse updatePosDraftInfo(Long id, OrderRequest request) {
        return posOrderService.updatePosDraftInfo(id, request);
    }

    @Transactional
    public OrderResponse updatePosDraftCustomer(Long id, OrderRequest request) {
        return posOrderService.updatePosDraftCustomer(id, request);
    }

    @Transactional
    public OrderResponse updatePosDraftShipping(Long id, OrderRequest request) {
        return posOrderService.updatePosDraftShipping(id, request);
    }

    @Transactional
    public OrderResponse updatePosDraftCoupon(Long id, String couponCode) {
        return posOrderService.updatePosDraftCoupon(id, couponCode);
    }

    @Transactional
    public void cancelPosDraft(Long id) {
        posOrderService.cancelPosDraft(id);
    }

    @Transactional
    public OrderResponse addItemToPosDraft(Long draftId, OrderItemRequest item) {
        log.info("OrderService::addItemToPosDraft - Start [draftId: {}]", draftId);

        // Load order and validate
        Order order = findOrder(draftId);
        validatePosDraft(order);

        // Delegate to POS service
        return posOrderService.addItemToDraft(draftId, item);
    }

    @Transactional
    public OrderResponse updateDraftItemQuantity(Long draftId, Long itemId, Integer quantity) {
        log.info("OrderService::updateDraftItemQuantity - Start [draftId: {}, itemId: {}, quantity: {}]",
                draftId, itemId, quantity);

        // Same validation as addItemToPosDraft
        Order order = findOrder(draftId);
        validatePosDraft(order);

        return posOrderService.updateDraftItemQuantity(draftId, itemId, quantity);
    }

    @Transactional
    public OrderResponse removeItemFromDraft(Long draftId, Long itemId) {
        log.info("OrderService::removeItemFromDraft - Start [draftId: {}, itemId: {}]", draftId, itemId);

        // Same validation as addItemToPosDraft
        Order order = findOrder(draftId);
        validatePosDraft(order);

        return posOrderService.removeItemFromDraft(draftId, itemId);
    }

    // Helper method to reduce duplication
    private void validatePosDraft(Order order) {
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

    @SuppressWarnings("unchecked")
    public List<OrderResponse> getPosDrafts() {
        List<OrderResponse> allDrafts = new ArrayList<>();
        
        // Get both INSTORE and DELIVERY drafts for current staff
        for (OrderType type : Arrays.asList(OrderType.POS_INSTORE, OrderType.POS_DELIVERY)) {
            OrderFilterRequest filter = new OrderFilterRequest();
            filter.setOrderType(type);
            filter.setStatus(OrderStatus.PENDING);
            filter.setStaffId(getUserId());
            
            PageResponse<?> page = onlineOrderService.getAllOrders(filter);
            allDrafts.addAll((List<OrderResponse>) page.getContents());
        }
        
        return allDrafts;
    }

    @Transactional
    public OrderResponse updateShippingInfo(Long id, ShippingInfoRequest request) {
        return onlineOrderService.updateShippingInfo(id, request);
    }

    @Transactional
    public OrderResponse confirmOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.confirmOrder(id);
        } else {
            return posOrderService.confirmOrder(id);
        }
    }

    @Transactional
    public OrderResponse shipOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.shipOrder(id);
        } else {
            return posOrderService.shipOrder(id);
        }
    }

    @Transactional
    public OrderResponse completeOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.completeOrder(id);
        } else {
            // POS orders don't have "complete" from SHIPPING state
            // They have completePosOrder() from PENDING state
            throw new InvalidRequestException(ErrorCodes.INVALID_REQUEST,
                "Use completePosOrder() endpoint for POS draft orders");
        }
    }

    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findOrder(id);
        if (order.getOrderType() == OrderType.ONLINE) {
            return onlineOrderService.cancelOrder(id);
        } else {
            // POS orders use cancelPosDraft()
            throw new InvalidRequestException(ErrorCodes.INVALID_REQUEST,
                "Use cancelPosDraft() endpoint for POS draft orders");
        }
    }
    
    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND, "Order not found"));
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}

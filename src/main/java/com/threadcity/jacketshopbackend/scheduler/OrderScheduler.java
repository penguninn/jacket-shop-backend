package com.threadcity.jacketshopbackend.scheduler;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderHistory;
import com.threadcity.jacketshopbackend.repository.OrderHistoryRepository;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import com.threadcity.jacketshopbackend.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Scheduler for automatic order management tasks.
 *
 * Primary function: Auto-cancel stale POS drafts to release reserved stock.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OrderScheduler {

    private final OrderRepository orderRepository;
    private final OrderHistoryRepository orderHistoryRepository;
    private final StockService stockService;

    /**
     * Timeout in minutes for POS drafts.
     * Default: 30 minutes
     */
    @Value("${order.pos.draft.timeout.minutes:30}")
    private int timeoutMinutes;

    /**
     * Auto-timeout stale POS drafts.
     * Runs every 5 minutes (300000ms) by default.
     *
     * This prevents reserved stock from being held indefinitely when staff
     * forgets to complete or cancel a POS draft.
     */
    @Scheduled(fixedDelayString = "${order.pos.draft.check.interval:300000}")
    @Transactional
    public void autoTimeoutPosDrafts() {
        log.debug("OrderScheduler::autoTimeoutPosDrafts - Starting check");

        Instant cutoff = Instant.now().minus(timeoutMinutes, ChronoUnit.MINUTES);

        List<Order> staleDrafts = orderRepository.findStalePosDrafts(
                OrderType.POS_INSTORE,
                OrderStatus.PENDING,
                cutoff
        );

        if (staleDrafts.isEmpty()) {
            log.debug("OrderScheduler::autoTimeoutPosDrafts - No stale drafts found");
            return;
        }

        log.info("OrderScheduler::autoTimeoutPosDrafts - Found {} stale POS drafts to cancel", staleDrafts.size());

        int successCount = 0;
        int failureCount = 0;

        for (Order draft : staleDrafts) {
            try {
                cancelStaleDraft(draft);
                successCount++;
            } catch (Exception e) {
                log.error("OrderScheduler::autoTimeoutPosDrafts - Failed to cancel draft {}: {}",
                        draft.getOrderCode(), e.getMessage());
                failureCount++;
            }
        }

        log.info("OrderScheduler::autoTimeoutPosDrafts - Completed. Cancelled: {}, Failed: {}",
                successCount, failureCount);
    }

    /**
     * Cancel a stale POS draft and release reserved stock.
     */
    private void cancelStaleDraft(Order draft) {
        log.info("OrderScheduler::cancelStaleDraft - Cancelling stale draft: {} (last updated: {})",
                draft.getOrderCode(), draft.getUpdatedAt());

        OrderStatus oldStatus = draft.getStatus();

        // Release reserved stock
        if (!draft.getOrderDetails().isEmpty()) {
            stockService.releaseReservedStock(new ArrayList<>(draft.getOrderDetails()));
            log.info("OrderScheduler::cancelStaleDraft - Released stock for {} items", draft.getOrderDetails().size());
        }

        // Cancel draft
        draft.setStatus(OrderStatus.CANCELLED);
        draft.setCancelledAt(Instant.now());
        orderRepository.save(draft);

        // Save history
        String note = String.format("Auto-cancelled: timeout after %d minutes of inactivity", timeoutMinutes);
        saveOrderHistory(draft, oldStatus, note);

        log.info("OrderScheduler::cancelStaleDraft - Successfully cancelled draft: {}", draft.getOrderCode());
    }

    /**
     * Save order history for auto-cancellation.
     */
    private void saveOrderHistory(Order order, OrderStatus oldStatus, String note) {
        OrderHistory history = OrderHistory.builder()
                .order(order)
                .oldStatus(oldStatus)
                .newStatus(order.getStatus())
                .oldPaymentStatus(order.getPaymentStatus())
                .newPaymentStatus(order.getPaymentStatus())
                .changedByUser(null) // System action
                .note(note)
                .build();
        orderHistoryRepository.save(history);
    }
}

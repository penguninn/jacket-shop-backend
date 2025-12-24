package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.core.Page;
import vn.payos.model.v1.payouts.GetPayoutListParams;
import vn.payos.model.v1.payouts.GetPayoutListParams.GetPayoutListParamsBuilder;
import vn.payos.model.v1.payouts.Payout;
import vn.payos.model.v1.payouts.PayoutApprovalState;
import vn.payos.model.v1.payouts.PayoutRequests;
import vn.payos.model.v1.payouts.batch.PayoutBatchItem;
import vn.payos.model.v1.payouts.batch.PayoutBatchRequest;
import vn.payos.model.v1.payoutsAccount.PayoutAccountInfo;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/payos/payouts")
@RequiredArgsConstructor
@Slf4j
public class PayOSPayoutController {

    private final PayOS payOS;

    @PostMapping("/create")
    public ApiResponse<?> create(@RequestBody PayoutRequests body) {
        log.info("PayOSPayoutController::create - Start");
        try {
            if (body.getReferenceId() == null || body.getReferenceId().isEmpty()) {
                body.setReferenceId("payout_" + (System.currentTimeMillis() / 1000));
            }

            Payout payout = payOS.payouts().create(body);
            log.info("PayOSPayoutController::create - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Payout created successfully")
                    .data(payout)
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("PayOSPayoutController::create - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @PostMapping("/batch/create")
    public ApiResponse<?> createBatch(@RequestBody PayoutBatchRequest body) {
        log.info("PayOSPayoutController::createBatch - Start");
        try {
            if (body.getReferenceId() == null || body.getReferenceId().isEmpty()) {
                body.setReferenceId("payout_" + (System.currentTimeMillis() / 1000));
            }

            List<PayoutBatchItem> payoutsList = body.getPayouts();
            if (payoutsList == null) {
                 return ApiResponse.builder()
                    .code(400)
                    .message("Payout list is null")
                    .timestamp(Instant.now())
                    .build();
            }
            for (int i = 0; i < payoutsList.size(); i++) {
                PayoutBatchItem batchItem = payoutsList.get(i);
                if (batchItem.getReferenceId() == null) {
                    batchItem.setReferenceId("payout_" + (System.currentTimeMillis() / 1000) + "_" + i);
                }
            }

            Payout payout = payOS.payouts().batch().create(body);
             log.info("PayOSPayoutController::createBatch - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Batch payout created successfully")
                    .data(payout)
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
             log.error("PayOSPayoutController::createBatch - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @GetMapping("/{payoutId}")
    public ApiResponse<?> retrieve(@PathVariable String payoutId) {
        log.info("PayOSPayoutController::retrieve - Start [payoutId: {}]", payoutId);
        try {
            Payout payout = payOS.payouts().get(payoutId);
            log.info("PayOSPayoutController::retrieve - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Get payout successfully")
                    .data(payout)
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("PayOSPayoutController::retrieve - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @GetMapping("/list")
    public ApiResponse<?> retrieveList(
            @RequestParam(required = false) String referenceId,
            @RequestParam(required = false) String approvalState,
            @RequestParam(required = false) List<String> category,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer offset) {
        log.info("PayOSPayoutController::retrieveList - Start");
        try {
            GetPayoutListParamsBuilder paramsBuilder =
                    GetPayoutListParams.builder()
                            .referenceId(referenceId)
                            .category(category)
                            .limit(limit)
                            .offset(offset);
            if (fromDate != null && !fromDate.isEmpty()) {
                paramsBuilder.fromDate(fromDate);
            }
            if (toDate != null && !toDate.isEmpty()) {
                paramsBuilder.toDate(toDate);
            }

            if (approvalState != null && !approvalState.isEmpty()) {
                try {
                    paramsBuilder.approvalState(PayoutApprovalState.valueOf(approvalState.toUpperCase()));
                } catch (IllegalArgumentException e) {
                     return ApiResponse.builder()
                            .code(400)
                            .message("Invalid approval state: " + approvalState)
                            .timestamp(Instant.now())
                            .build();
                }
            }

            GetPayoutListParams params = paramsBuilder.build();

            List<Payout> data = new ArrayList<>();
            Page<Payout> page = payOS.payouts().list(params);
            page.autoPager().stream().forEach(data::add);
            
            log.info("PayOSPayoutController::retrieveList - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Get payout list successfully")
                    .data(data)
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("PayOSPayoutController::retrieveList - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }

    @GetMapping("/balance")
    public ApiResponse<?> getAccountBalance() {
        log.info("PayOSPayoutController::getAccountBalance - Start");
        try {
            PayoutAccountInfo accountInfo = payOS.payoutsAccount().balance();
            log.info("PayOSPayoutController::getAccountBalance - Success");
            return ApiResponse.builder()
                    .code(200)
                    .message("Get balance successfully")
                    .data(accountInfo)
                    .timestamp(Instant.now())
                    .build();

        } catch (Exception e) {
            log.error("PayOSPayoutController::getAccountBalance - Error: {}", e.getMessage(), e);
            return ApiResponse.builder()
                    .code(500)
                    .message(e.getMessage())
                    .timestamp(Instant.now())
                    .build();
        }
    }
}

package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.common.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewCreateRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewFilterRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewUpdateRequest;
import com.threadcity.jacketshopbackend.dto.review.response.ReviewListResponse;
import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    // ================= CREATE =================
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<ReviewResponse> createReview(
            @Valid @RequestBody ReviewCreateRequest request) {

        log.info("ReviewController::createReview - Execution started");

        ReviewResponse response = reviewService.createReview(request);

        log.info("ReviewController::createReview - Execution completed");

        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Review created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
//    @PreAuthorize("hasRole('CUSTOMER')")
    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','ADMIN')")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {

        log.info("ReviewController::updateReview - Execution started. [id: {}]", id);

        ReviewResponse response = reviewService.updateReviewById(request, id);

        log.info("ReviewController::updateReview - Execution completed. [id: {}]", id);

        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Review updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','ADMIN','STAFF')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {

        log.info("ReviewController::deleteReview - Execution started. [id: {}]", id);

        reviewService.deleteReview(id);

        log.info("ReviewController::deleteReview - Execution completed. [id: {}]", id);

        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Review deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }

    // ================= GET BY PRODUCT (PUBLIC) =================
    @GetMapping("/product/{productId}")
    public ApiResponse<PageResponse<ReviewListResponse>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("ReviewController::getReviewsByProduct - Execution started. [productId: {}]", productId);

        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .productId(productId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<ReviewListResponse> response = reviewService.getAllReviews(filter);

        log.info("ReviewController::getReviewsByProduct - Execution completed. [productId: {}]", productId);

        return ApiResponse.<PageResponse<ReviewListResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("Reviews retrieved successfully for product.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ================= GET ALL (ADMIN ONLY) =================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ReviewListResponse>> getAllReviews(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        log.info("ReviewController::getAllReviews - Execution started");

        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .productId(productId)
                .rating(rating)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<ReviewListResponse> response = reviewService.getAllReviews(filter);

        log.info("ReviewController::getAllReviews - Execution completed");

        return ApiResponse.<PageResponse<ReviewListResponse>>builder()
                .code(HttpStatus.OK.value())
                .message("All reviews retrieved successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
}

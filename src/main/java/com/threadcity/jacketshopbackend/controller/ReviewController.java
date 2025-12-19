package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.request.ReviewRequest;
import com.threadcity.jacketshopbackend.dto.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ReviewResponse;
import com.threadcity.jacketshopbackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ApiResponse<?> createReview(@Valid @RequestBody ReviewRequest request) {
        log.info("ReviewController::createReview - Execution started.");
        ReviewResponse response = reviewService.createReview(request);
        log.info("ReviewController::createReview - Execution completed.");
        return ApiResponse.builder()
                .code(201)
                .message("Review created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @GetMapping("/product/{productId}")
    public ApiResponse<?> getReviewsByProductId(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("ReviewController::getReviewsByProductId - Execution started. [productId: {}]", productId);
        PageResponse<?> response = reviewService.getReviewsByProductId(productId, page, size);
        log.info("ReviewController::getReviewsByProductId - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Get reviews successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> deleteReview(@PathVariable Long id) {
        log.info("ReviewController::deleteReview - Execution started. [id: {}]", id);
        reviewService.deleteReview(id);
        log.info("ReviewController::deleteReview - Execution completed.");
        return ApiResponse.builder()
                .code(200)
                .message("Review deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }
}

package com.threadcity.jacketshopbackend.controller;

import com.threadcity.jacketshopbackend.dto.common.response.ApiResponse;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewCreateRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewFilterRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewUpdateRequest;
import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

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

        ReviewResponse response = reviewService.createReview(request);

        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.CREATED.value())
                .message("Review created successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ================= UPDATE =================
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','ADMIN')")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ReviewUpdateRequest request) {

        ReviewResponse response = reviewService.updateReviewById(request, id);

        return ApiResponse.<ReviewResponse>builder()
                .code(HttpStatus.OK.value())
                .message("Review updated successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ================= DELETE =================
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER','STAFF','ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ApiResponse<Void> deleteReview(@PathVariable Long id) {

        reviewService.deleteReview(id);

        return ApiResponse.<Void>builder()
                .code(HttpStatus.NO_CONTENT.value())
                .message("Review deleted successfully.")
                .timestamp(Instant.now())
                .build();
    }

    // ================= GET BY PRODUCT =================
    @GetMapping("/product/{productId}")
    public ApiResponse<PageResponse<List<ReviewResponse>>> getReviewsByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .productId(productId)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<List<ReviewResponse>> response =
                reviewService.getAllReviews(filter);

        return ApiResponse.<PageResponse<List<ReviewResponse>>>builder()
                .code(HttpStatus.OK.value())
                .message("Reviews retrieved successfully for product.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }

    // ================= GET ALL (ADMIN) =================
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<List<ReviewResponse>>> getAllReviews(
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {

        ReviewFilterRequest filter = ReviewFilterRequest.builder()
                .productId(productId)
                .rating(rating)
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .build();

        PageResponse<List<ReviewResponse>> response =
                reviewService.getAllReviews(filter);

        return ApiResponse.<PageResponse<List<ReviewResponse>>>builder()
                .code(HttpStatus.OK.value())
                .message("All reviews retrieved successfully.")
                .data(response)
                .timestamp(Instant.now())
                .build();
    }
}

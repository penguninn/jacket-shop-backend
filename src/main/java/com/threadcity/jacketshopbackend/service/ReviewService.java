package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.common.Enums;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewCreateRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewFilterRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewUpdateRequest;
import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.Review;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceConflictException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ReviewRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import com.threadcity.jacketshopbackend.specification.ReviewSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    // ================== GET ALL ==================
 public PageResponse<List<ReviewResponse>> getAllReviews(ReviewFilterRequest request)
    {

        Sort sort = Sort.by(
                Sort.Direction.fromString(request.getSortDir()),
                request.getSortBy()
        );

        Pageable pageable = PageRequest.of(
                request.getPage(),
                request.getSize(),
                sort
        );

        Specification<Review> spec = ReviewSpecification.buildSpec(request);
        Page<Review> page = reviewRepository.findAll(spec, pageable);

        List<ReviewResponse> contents = page.getContent()
                .stream()
                .map(this::toReviewResponse)
                .toList();

        return PageResponse.<List<ReviewResponse>>builder()
                .contents(contents)
                .page(page.getNumber())
                .size(page.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }


    // ================== CREATE ==================
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest req) {

        Long currentUserId = getCurrentUserId();

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + req.getProductId()));

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.ORDER_NOT_FOUND,
                        "Order not found with id: " + req.getOrderId()));

        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("You can only rate your own orders.");
        }

        if (order.getStatus() != Enums.OrderStatus.COMPLETED) {
            throw new AccessDeniedException("Reviews can only be given after the order is completed.");
        }

        boolean hasProduct = order.getOrderDetails().stream()
                .anyMatch(d ->
                        d.getProductVariant() != null &&
                                d.getProductVariant().getProduct().getId().equals(req.getProductId())
                );

        if (!hasProduct) {
            throw new AccessDeniedException("The product does not exist in the order.");
        }

        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(
                currentUserId, req.getProductId(), req.getOrderId())) {
            throw new ResourceConflictException(
                    ErrorCodes.REVIEW_ALREADY_EXISTS,
                    "You have already reviewed this product.");
        }

        Review review = Review.builder()
                .user(order.getUser())
                .product(product)
                .order(order)
                .rating(req.getRating())
                .comment(req.getComment())
                .userName(order.getCustomerName())
                .productName(product.getName())
                .build();

        Review saved = reviewRepository.save(review);
        updateProductRating(product.getId());

        return toReviewResponse(saved);
    }

    // ================== UPDATE ==================
    @Transactional
    public ReviewResponse updateReviewById(ReviewUpdateRequest req, Long id) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));

        Long currentUserId = getCurrentUserId();
        boolean isAdmin = hasRole("ADMIN");

        if (!review.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("No editing rights");
        }

        if (req.getRating() != null) review.setRating(req.getRating());
        if (req.getComment() != null) review.setComment(req.getComment());

        Review saved = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());

        return toReviewResponse(saved);
    }

    // ================== DELETE ==================
    @Transactional
    public void deleteReview(Long id) {

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));

        Long currentUserId = getCurrentUserId();
        boolean isAdmin = hasRole("ADMIN");

        if (!review.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("No deletion rights");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        updateProductRating(productId);
    }

    // ================== MAPPER ==================
    private ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct().getId())
                .productName(review.getProductName())
                .userId(review.getUser().getId())
                .userName(review.getUserName())
                .orderId(review.getOrder() != null ? review.getOrder().getId() : null)
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt() != null
                        ? LocalDateTime.ofInstant(review.getCreatedAt(), ZoneId.systemDefault())
                        : null)
                .updatedAt(review.getUpdatedAt() != null
                        ? LocalDateTime.ofInstant(review.getUpdatedAt(), ZoneId.systemDefault())
                        : null)
                .build();
    }

    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + productId));

        List<Review> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            product.setRatingCount(0);
            product.setRatingAverage(BigDecimal.ZERO);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0);
            product.setRatingCount(reviews.size());
            product.setRatingAverage(
                    BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP)
            );
        }

        productRepository.save(product);
    }

    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetailsImpl) auth.getPrincipal()).getId();
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(role) || r.equals("ROLE_" + role));
    }
}

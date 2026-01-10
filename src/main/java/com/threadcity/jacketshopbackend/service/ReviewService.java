package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewCreateRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewUpdateRequest;
import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.Review;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewFilterRequest;
import com.threadcity.jacketshopbackend.mapper.ReviewMapper;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ReviewRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import com.threadcity.jacketshopbackend.specification.ReviewSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    public PageResponse<?> getAllReviews(ReviewFilterRequest request) {
        log.info("ReviewService::getAllReviews - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Review> spec = ReviewSpecification.buildSpec(request);
        Page<Review> page = reviewRepository.findAll(spec, pageable);

        log.info("ReviewService::getAllReviews - Execution completed.");
        return PageResponse.builder()
                .contents(page.getContent().stream().map(reviewMapper::toDto).toList())
                .page(request.getPage())
                .size(request.getSize())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();
    }

    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest req) {
        log.info("ReviewService::createReview - Execution started.");

        Long currentUserId = getCurrentUserId();

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + req.getProductId()));

        Order order = orderRepository.findById(req.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND,
                        "Order not found with id: " + req.getOrderId()));

        // Validate: order phải thuộc về user hiện tại
        if (!order.getUser().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Bạn chỉ được đánh giá đơn hàng của mình");
        }

        // Validate: order phải đã COMPLETED
        if (order.getStatus() != com.threadcity.jacketshopbackend.common.Enums.OrderStatus.COMPLETED) {
            throw new AccessDeniedException("Chỉ được đánh giá sau khi đơn hàng đã hoàn thành");
        }

        // Validate: sản phẩm có trong đơn hàng không
        boolean hasProduct = order.getOrderDetails().stream()
                .anyMatch(detail -> detail.getProductVariant() != null &&
                        detail.getProductVariant().getProduct().getId().equals(req.getProductId()));

        if (!hasProduct) {
            throw new AccessDeniedException("Sản phẩm này không có trong đơn hàng");
        }

        // Validate: chưa review cho cùng product + order
        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(currentUserId, req.getProductId(), req.getOrderId())) {
            throw new AccessDeniedException("Bạn đã đánh giá sản phẩm này trong đơn hàng rồi");
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

        log.info("ReviewService::createReview - Execution completed. [reviewId: {}]", saved.getId());
        return reviewMapper.toDto(saved);
    }

    @Transactional
    public ReviewResponse updateReviewById(ReviewUpdateRequest req, Long id) {
        log.info("ReviewService::updateReviewById - Execution started. [id: {}]", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));

        Long currentUserId = getCurrentUserId();
        boolean isAdmin = hasRole("ADMIN");

        if (!review.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền sửa đánh giá này");
        }

        if (req.getRating() != null) review.setRating(req.getRating());
        if (req.getComment() != null) review.setComment(req.getComment());

        Review saved = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());

        log.info("ReviewService::updateReviewById - Execution completed.");
        return reviewMapper.toDto(saved);
    }

    @Transactional
    public void deleteReview(Long id) {
        log.info("ReviewService::deleteReview - Execution started. [id: {}]", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));

        Long currentUserId = getCurrentUserId();
        boolean isAdmin = hasRole("ADMIN");

        if (!review.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new AccessDeniedException("Bạn không có quyền xóa đánh giá này");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);
        updateProductRating(productId);

        log.info("ReviewService::deleteReview - Execution completed.");
    }

    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();

        List<Review> reviews = reviewRepository.findByProductId(productId);

        if (reviews.isEmpty()) {
            product.setRatingCount(0);
            product.setRatingAverage(BigDecimal.ZERO);
        } else {
            double avg = reviews.stream().mapToInt(Review::getRating).average().orElse(0.0);
            product.setRatingCount(reviews.size());
            product.setRatingAverage(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP));
        }

        productRepository.save(product);
    }

    private Long getCurrentUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }

    // =========================
    // Method bổ sung để fix lỗi compile hasRole
    // =========================
    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getAuthorities() == null) return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(r -> r.equals(role) || r.equals("ROLE_" + role));
    }
}

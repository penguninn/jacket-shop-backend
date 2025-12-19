package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.request.ReviewRequest;
import com.threadcity.jacketshopbackend.dto.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.Review;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.AppException;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.mapper.ReviewMapper;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ReviewRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse createReview(ReviewRequest request) {
        log.info("ReviewService::createReview - Execution started.");
        Long userId = getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found"));

        Review review = Review.builder()
                .user(user)
                .userName(user.getFullName())
                .product(product)
                .productName(product.getName())
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        if (request.getOrderId() != null) {
            Order order = orderRepository.findById(request.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Order not found"));
            review.setOrder(order);
        }

        Review savedReview = reviewRepository.save(review);
        updateProductRating(product);

        log.info("ReviewService::createReview - Execution completed.");
        return reviewMapper.toDto(savedReview);
    }

    public PageResponse<?> getReviewsByProductId(Long productId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);
        
        List<ReviewResponse> contents = reviewPage.getContent().stream()
                .map(reviewMapper::toDto)
                .toList();

        return PageResponse.builder()
                .contents(contents)
                .page(page)
                .size(size)
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .build();
    }

    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Review not found"));
        
        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    private void updateProductRating(Product product) {
        // This is a naive implementation, ideally use a native query or aggregate
        Page<Review> reviews = reviewRepository.findByProductId(product.getId(), Pageable.unpaged());
        List<Review> allReviews = reviews.getContent();
        
        if (allReviews.isEmpty()) {
            product.setRatingAverage(BigDecimal.ZERO);
            product.setRatingCount(0);
        } else {
            double average = allReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            product.setRatingAverage(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
            product.setRatingCount(allReviews.size());
        }
        productRepository.save(product);
    }

    private Long getUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
    }
}

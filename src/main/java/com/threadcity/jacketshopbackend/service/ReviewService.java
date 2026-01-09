//package com.threadcity.jacketshopbackend.service;
//
//import com.threadcity.jacketshopbackend.dto.product.request.ReviewRequest;
//import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
//import com.threadcity.jacketshopbackend.dto.product.response.ReviewResponse;
//import com.threadcity.jacketshopbackend.entity.Order;
//import com.threadcity.jacketshopbackend.entity.Product;
//import com.threadcity.jacketshopbackend.entity.Review;
//import com.threadcity.jacketshopbackend.entity.User;
//import com.threadcity.jacketshopbackend.exception.ErrorCodes;
//import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
//import com.threadcity.jacketshopbackend.mapper.ReviewMapper;
//import com.threadcity.jacketshopbackend.repository.OrderRepository;
//import com.threadcity.jacketshopbackend.repository.ProductRepository;
//import com.threadcity.jacketshopbackend.repository.ReviewRepository;
//import com.threadcity.jacketshopbackend.repository.UserRepository;
//import com.threadcity.jacketshopbackend.utils.SecurityUtils;
//import com.threadcity.jacketshopbackend.service.auth.UserDetailsImpl;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.math.RoundingMode;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ReviewService {
//
//    private final ReviewRepository reviewRepository;
//    private final ProductRepository productRepository;
//    private final UserRepository userRepository;
//    private final OrderRepository orderRepository;
//    private final ReviewMapper reviewMapper;
//
//    @Transactional
//    public ReviewResponse createReview(ReviewRequest request) {
//        log.info("ReviewService::createReview - Execution started.");
//        Long userId = getUserId();
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));
//
//        Product product = productRepository.findById(request.getProductId())
//                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Product not found"));
//
//        Review review = Review.builder()
//                .user(user)
//                .userName(user.getFullName())
//                .product(product)
//                .productName(product.getName())
//                .rating(request.getRating())
//                .comment(request.getComment())
//                .build();
//
//        if (request.getOrderId() != null) {
//            Order order = orderRepository.findById(request.getOrderId())
//                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Order not found"));
//            review.setOrder(order);
//        }
//
//        Review savedReview = reviewRepository.save(review);
//        updateProductRating(product);
//
//        log.info("ReviewService::createReview - Execution completed.");
//        return reviewMapper.toDto(savedReview);
//    }
//
//    // --- MỚI THÊM: Logic lấy tất cả review ---
//    public PageResponse<?> getAllReviews(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<Review> reviewPage = reviewRepository.findAll(pageable);
//
//        List<ReviewResponse> contents = reviewPage.getContent().stream()
//                .map(reviewMapper::toDto)
//                .toList();
//
//        return PageResponse.builder()
//                .contents(contents)
//                .page(page)
//                .size(size)
//                .totalElements(reviewPage.getTotalElements())
//                .totalPages(reviewPage.getTotalPages())
//                .build();
//    }
//    // ----------------------------------------
//
//    public PageResponse<?> getReviewsByProductId(Long productId, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<Review> reviewPage = reviewRepository.findByProductId(productId, pageable);
//
//        List<ReviewResponse> contents = reviewPage.getContent().stream()
//                .map(reviewMapper::toDto)
//                .toList();
//
//        return PageResponse.builder()
//                .contents(contents)
//                .page(page)
//                .size(size)
//                .totalElements(reviewPage.getTotalElements())
//                .totalPages(reviewPage.getTotalPages())
//                .build();
//    }
//
//    @Transactional
//    public void deleteReview(Long id) {
//        Review review = reviewRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND, "Review not found"));
//
//        // Security: Verify ownership (user owns review OR is admin/staff)
//        SecurityUtils.requireOwnership(review.getUser().getId(), "review");
//
//        Product product = review.getProduct();
//        reviewRepository.delete(review);
//        updateProductRating(product);
//    }
//
//    private void updateProductRating(Product product) {
//        Page<Review> reviews = reviewRepository.findByProductId(product.getId(), Pageable.unpaged());
//        List<Review> allReviews = reviews.getContent();
//
//        if (allReviews.isEmpty()) {
//            product.setRatingAverage(BigDecimal.ZERO);
//            product.setRatingCount(0);
//        } else {
//            double average = allReviews.stream()
//                    .mapToInt(Review::getRating)
//                    .average()
//                    .orElse(0.0);
//            product.setRatingAverage(BigDecimal.valueOf(average).setScale(1, RoundingMode.HALF_UP));
//            product.setRatingCount(allReviews.size());
//        }
//        productRepository.save(product);
//    }
//
//    private Long getUserId() {
//        return ((UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getId();
//    }
//    public PageResponse<?> searchReviews(
//            String keyword,
//            Integer rating,
//            int page,
//            int size
//    ) {
//        Pageable pageable = PageRequest.of(
//                page,
//                size,
//                Sort.by("createdAt").descending()
//        );
//
//        // Nếu keyword rỗng → null
//        if (keyword != null && keyword.trim().isEmpty()) {
//            keyword = null;
//        }
//
//        Page<Review> reviewPage =
//                reviewRepository.searchReviews(keyword, rating, pageable);
//
//        List<ReviewResponse> contents = reviewPage.getContent()
//                .stream()
//                .map(reviewMapper::toDto)
//                .toList();
//
//        return PageResponse.builder()
//                .contents(contents)
//                .page(page)
//                .size(size)
//                .totalElements(reviewPage.getTotalElements())
//                .totalPages(reviewPage.getTotalPages())
//                .build();
//    }
//    public PageResponse<?> getReviewsByProductId(
//            Long productId,
//            int page,
//            int size,
//            String sort
//    ) {
//        Sort sortBy = sort.equalsIgnoreCase("oldest")
//                ? Sort.by("createdAt").ascending()
//                : Sort.by("createdAt").descending(); // default latest
//
//        Pageable pageable = PageRequest.of(page, size, sortBy);
//
//        Page<Review> reviewPage =
//                reviewRepository.findByProductId(productId, pageable);
//
//        List<ReviewResponse> contents = reviewPage.getContent()
//                .stream()
//                .map(reviewMapper::toDto)
//                .toList();
//
//        return PageResponse.builder()
//                .contents(contents)
//                .page(page)
//                .size(size)
//                .totalElements(reviewPage.getTotalElements())
//                .totalPages(reviewPage.getTotalPages())
//                .build();
//    }
//    public PageResponse<?> searchReviews(
//            String keyword,
//            Integer rating,
//            int page,
//            int size,
//            String sort
//    ) {
//        Sort sortBy = sort.equalsIgnoreCase("oldest")
//                ? Sort.by("createdAt").ascending()
//                : Sort.by("createdAt").descending();
//
//        Pageable pageable = PageRequest.of(page, size, sortBy);
//
//        if (keyword != null && keyword.trim().isEmpty()) {
//            keyword = null;
//        }
//
//        Page<Review> reviewPage =
//                reviewRepository.searchReviews(keyword, rating, pageable);
//
//        List<ReviewResponse> contents = reviewPage.getContent()
//                .stream()
//                .map(reviewMapper::toDto)
//                .toList();
//
//        return PageResponse.builder()
//                .contents(contents)
//                .page(page)
//                .size(size)
//                .totalElements(reviewPage.getTotalElements())
//                .totalPages(reviewPage.getTotalPages())
//                .build();
//    }
//
//}


package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewCreateRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewUpdateRequest;
import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.OrderDetail;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.Review;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ForbiddenException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.ReviewFilterRequest;
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
            throw new ForbiddenException(ErrorCodes.REVIEW_FORBIDDEN, "Bạn chỉ được đánh giá đơn hàng của mình");
        }

        // Validate: order phải đã COMPLETED
        if (order.getStatus() != com.threadcity.jacketshopbackend.common.Enums.OrderStatus.COMPLETED) {
            throw new ForbiddenException(ErrorCodes.REVIEW_ORDER_NOT_COMPLETED,
                    "Chỉ được đánh giá sau khi đơn hàng đã hoàn thành");
        }

        // Validate: sản phẩm có trong đơn hàng không
        boolean hasProduct = order.getOrderDetails().stream()
                .anyMatch(detail -> detail.getProductVariant() != null &&
                        detail.getProductVariant().getProduct().getId().equals(req.getProductId()));

        if (!hasProduct) {
            throw new ForbiddenException(ErrorCodes.REVIEW_PRODUCT_NOT_IN_ORDER,
                    "Sản phẩm này không có trong đơn hàng");
        }

        // Validate: chưa review cho cùng product + order
        if (reviewRepository.existsByUserIdAndProductIdAndOrderId(currentUserId, req.getProductId(), req.getOrderId())) {
            throw new ForbiddenException(ErrorCodes.REVIEW_ALREADY_EXISTS,
                    "Bạn đã đánh giá sản phẩm này trong đơn hàng rồi");
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
            throw new ForbiddenException(ErrorCodes.REVIEW_FORBIDDEN, "Bạn không có quyền sửa đánh giá này");
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
            throw new ForbiddenException(ErrorCodes.REVIEW_FORBIDDEN, "Bạn không có quyền xóa đánh giá này");
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

    private boolean hasRole(String role) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.common.request.BulkDeleteRequest;
import com.threadcity.jacketshopbackend.dto.common.request.UpdateStatusRequest;
import com.threadcity.jacketshopbackend.dto.common.response.PageResponse;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewCreateRequest;
import com.threadcity.jacketshopbackend.dto.review.request.ReviewUpdateRequest;
import com.threadcity.jacketshopbackend.dto.review.response.ReviewResponse;
import com.threadcity.jacketshopbackend.entity.Order;
import com.threadcity.jacketshopbackend.entity.Product;
import com.threadcity.jacketshopbackend.entity.Review;
import com.threadcity.jacketshopbackend.entity.User;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ForbiddenException;
import com.threadcity.jacketshopbackend.exception.ResourceNotFoundException;
import com.threadcity.jacketshopbackend.filter.ReviewFilterRequest;
import com.threadcity.jacketshopbackend.mapper.ReviewMapper;
import com.threadcity.jacketshopbackend.repository.OrderRepository;
import com.threadcity.jacketshopbackend.repository.ProductRepository;
import com.threadcity.jacketshopbackend.repository.ReviewRepository;
import com.threadcity.jacketshopbackend.repository.UserRepository;
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
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final ReviewMapper reviewMapper;

    // Lấy danh sách review (admin hoặc public - tùy filter)
    public PageResponse<?> getAllReviews(ReviewFilterRequest request) {
        log.info("ReviewService::getAllReviews - Execution started.");

        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortDir()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        Specification<Review> spec = ReviewSpecification.buildSpec(request);
        Page<Review> reviewPage = reviewRepository.findAll(spec, pageable);

        List<ReviewResponse> responses = reviewPage.getContent().stream()
                .map(reviewMapper::toDto)
                .toList();

        log.info("ReviewService::getAllReviews - Execution completed.");
        return PageResponse.builder()
                .contents(responses)
                .size(request.getSize())
                .page(request.getPage())
                .totalPages(reviewPage.getTotalPages())
                .totalElements(reviewPage.getTotalElements())
                .build();
    }

    // Lấy review theo ID (admin hoặc owner)
    public ReviewResponse getReviewById(Long id) {
        log.info("ReviewService::getReviewById - Execution started. [id: {}]", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));
        log.info("ReviewService::getReviewById - Execution completed. [id: {}]", id);
        return reviewMapper.toDto(review);
    }

    // User tạo review mới (chỉ sau khi mua hàng)
    @Transactional
    public ReviewResponse createReview(ReviewCreateRequest req) {
        log.info("ReviewService::createReview - Execution started.");

        Long currentUserId = getCurrentUserId();

        Product product = productRepository.findById(req.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.PRODUCT_NOT_FOUND,
                        "Product not found with id: " + req.getProductId()));

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.USER_NOT_FOUND, "User not found"));

        Order order = null;
        if (req.getOrderId() != null) {
            order = orderRepository.findById(req.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.ORDER_NOT_FOUND,
                            "Order not found with id: " + req.getOrderId()));

            // Kiểm tra order thuộc về user hiện tại và đã hoàn thành
            if (!order.getUser().getId().equals(currentUserId)) {
                throw new ForbiddenException(ErrorCodes.FORBIDDEN, "Bạn chỉ được đánh giá đơn hàng của mình");
            }

            // Giả sử bạn có enum OrderStatus.COMPLETED hoặc DELIVERED
            // if (!order.getStatus().isReviewable()) { ... }

            // Kiểm tra sản phẩm có trong order không
            boolean hasProduct = order.getOrderItems().stream()
                    .anyMatch(item -> item.getProductVariant().getProduct().getId().equals(req.getProductId()));
            if (!hasProduct) {
                throw new ForbiddenException(ErrorCodes.INVALID_REQUEST, "Sản phẩm không thuộc đơn hàng này");
            }
        }

        Review review = Review.builder()
                .user(user)
                .product(product)
                .order(order)
                .rating(req.getRating())
                .comment(req.getComment())
                .userName(user.getFullName())           // Lưu snapshot tên user
                .productName(product.getName())         // Lưu snapshot tên sản phẩm
                .build();

        Review saved = reviewRepository.save(review);

        // Cập nhật rating trung bình cho product
        updateProductRating(product.getId());

        log.info("ReviewService::createReview - Execution completed. [reviewId: {}]", saved.getId());
        return reviewMapper.toDto(saved);
    }

    // User hoặc admin sửa review
    @Transactional
    public ReviewResponse updateReviewById(ReviewUpdateRequest req, Long id) {
        log.info("ReviewService::updateReviewById - Execution started. [id: {}]", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));

        Long currentUserId = getCurrentUserId();
        boolean isAdmin = hasRole("ADMIN"); // hoặc check authority
        if (!review.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException(ErrorCodes.FORBIDDEN, "Bạn không có quyền sửa đánh giá này");
        }

        if (req.getRating() != null) review.setRating(req.getRating());
        if (req.getComment() != null) review.setComment(req.getComment());

        Review saved = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());

        log.info("ReviewService::updateReviewById - Execution completed. [id: {}]", id);
        return reviewMapper.toDto(saved);
    }

    // Admin thay đổi trạng thái (nếu có duyệt review)
    @Transactional
    public ReviewResponse updateReviewStatus(UpdateStatusRequest request, Long id) {
        log.info("ReviewService::updateReviewStatus - Execution started. [id: {}]", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));

        // Chỉ admin mới được duyệt
        if (!hasRole("ADMIN")) {
            throw new ForbiddenException(ErrorCodes.FORBIDDEN, "Chỉ admin mới được duyệt đánh giá");
        }

        // Nếu bạn thêm trường isApproved
        // review.setIsApproved(request.getStatus() == Enums.Status.ACTIVE);

        Review saved = reviewRepository.save(review);
        updateProductRating(review.getProduct().getId());

        log.info("ReviewService::updateReviewStatus - Execution completed. [id: {}]", id);
        return reviewMapper.toDto(saved);
    }

    // Xóa review (owner hoặc admin)
    @Transactional
    public void deleteReview(Long id) {
        log.info("ReviewService::deleteReview - Execution started. [id: {}]", id);

        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCodes.REVIEW_NOT_FOUND,
                        "Review not found with id: " + id));

        Long currentUserId = getCurrentUserId();
        boolean isAdmin = hasRole("ADMIN");
        if (!review.getUser().getId().equals(currentUserId) && !isAdmin) {
            throw new ForbiddenException(ErrorCodes.FORBIDDEN, "Bạn không có quyền xóa đánh giá này");
        }

        Long productId = review.getProduct().getId();
        reviewRepository.delete(review);

        updateProductRating(productId);

        log.info("ReviewService::deleteReview - Execution completed. [id: {}]", id);
    }

    // Bulk delete (admin)
    @Transactional
    public void bulkDeleteReviews(BulkDeleteRequest request) {
        log.info("ReviewService::bulkDeleteReviews - Execution started.");

        List<Review> reviews = reviewRepository.findAllById(request.getIds());
        if (reviews.size() != request.getIds().size()) {
            Set<Long> foundIds = reviews.stream().map(Review::getId).collect(Collectors.toSet());
            Set<Long> missingIds = Set.copyOf(request.getIds());
            missingIds.removeAll(foundIds);
            throw new ResourceNotFoundException(ErrorCodes.REVIEW_NOT_FOUND, "Reviews not found: " + missingIds);
        }

        Set<Long> productIds = reviews.stream()
                .map(r -> r.getProduct().getId())
                .collect(Collectors.toSet());

        reviewRepository.deleteAllInBatch(reviews);

        // Cập nhật lại rating cho tất cả product bị ảnh hưởng
        productIds.forEach(this::updateProductRating);

        log.info("ReviewService::bulkDeleteReviews - Execution completed.");
    }

    // Phương thức private: cập nhật ratingAverage và ratingCount cho Product
    private void updateProductRating(Long productId) {
        Product product = productRepository.findById(productId).orElseThrow();

        List<Review> approvedReviews = reviewRepository.findByProductId(productId);
        // Nếu bạn có isApproved = true thì thêm điều kiện: findByProductIdAndIsApprovedTrue(productId)

        if (approvedReviews.isEmpty()) {
            product.setRatingCount(0);
            product.setRatingAverage(BigDecimal.ZERO);
        } else {
            double avg = approvedReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);

            product.setRatingCount(approvedReviews.size());
            product.setRatingAverage(BigDecimal.valueOf(avg)
                    .setScale(1, RoundingMode.HALF_UP));
        }

        productRepository.save(product);
    }

    // Helper methods
    private Long getCurrentUserId() {
        return ((UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal()).getId();
    }

    private boolean hasRole(String roleName) {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + roleName));
    }
}
package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Order extends BaseEntity {

    @Column(name = "order_code", nullable = false, length = 32, unique = true)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "customer_name", nullable = false, length = 120, columnDefinition = "NVARCHAR(120)")
    private String customerName;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "shipping_recipient_name", length = 120, columnDefinition = "NVARCHAR(120)")
    private String shippingRecipientName;

    @Column(name = "shipping_recipient_phone", length = 20)
    private String shippingRecipientPhone;

    @Column(name = "shipping_address_line", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String shippingAddressLine;

    @Column(name = "shipping_province_code", length = 20)
    private String shippingProvinceCode;

    @Column(name = "shipping_district_code", length = 20)
    private String shippingDistrictCode;

    @Column(name = "shipping_ward_code", length = 20)
    private String shippingWardCode;

    @Column(name = "shipping_province_name", columnDefinition = "NVARCHAR(255)")
    private String shippingProvinceName;

    @Column(name = "shipping_district_name", columnDefinition = "NVARCHAR(255)")
    private String shippingDistrictName;

    @Column(name = "shipping_ward_name", columnDefinition = "NVARCHAR(255)")
    private String shippingWardName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod; // cod, stripe, qr_online, cash, or_pos

    @Column(name = "payment_method_name", length = 80, columnDefinition = "NVARCHAR(80)")
    private String paymentMethodName;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shipping_method_id", nullable = false)
    private ShippingMethod shippingMethod; // VTPOST, EMS, VNPOST, GHTK, GHNV3, SPX

    @Column(name = "shipping_method_name", length = 100, columnDefinition = "NVARCHAR(100)")
    private String shippingMethodName;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingFee = BigDecimal.ZERO;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "payment_date")
    private Instant paymentDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderDetail> details = new ArrayList<>();
}

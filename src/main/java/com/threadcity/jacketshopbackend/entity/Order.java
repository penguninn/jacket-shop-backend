package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders", schema = "dbo", indexes = {
        @Index(name = "IX_orders_code", columnList = "order_code"),
        @Index(name = "IX_orders_user", columnList = "user_id, created_at"),
        @Index(name = "IX_orders_staff", columnList = "staff_id, created_at"),
        @Index(name = "IX_orders_status", columnList = "status, created_at"),
        @Index(name = "IX_orders_payment_status", columnList = "payment_status, status"),
        @Index(name = "IX_orders_type", columnList = "order_type, status"),
        @Index(name = "IX_orders_created", columnList = "created_at"),
        @Index(name = "IX_orders_tracking", columnList = "tracking_number")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_orders_code", columnNames = { "order_code" })
})
public class Order extends BaseEntity {

    @Size(max = 32)
    @NotNull
    @Column(name = "order_code", nullable = false, length = 32)
    private String orderCode;

    @Size(max = 20)
    @NotNull
    @Column(name = "order_type", nullable = false, length = 20)
    private String orderType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "staff_id")
    private User staff;

    @Size(max = 120)
    @NotNull
    @Nationalized
    @Column(name = "customer_name", nullable = false, length = 120)
    private String customerName;

    @Size(max = 15)
    @NotNull
    @Column(name = "customer_phone", nullable = false, length = 15)
    private String customerPhone;

    @Size(max = 255)
    @Column(name = "customer_email")
    private String customerEmail;

    @Size(max = 120)
    @Nationalized
    @Column(name = "shipping_recipient_name", length = 120)
    private String shippingRecipientName;

    @Size(max = 15)
    @Column(name = "shipping_recipient_phone", length = 15)
    private String shippingRecipientPhone;

    @Size(max = 255)
    @Nationalized
    @Column(name = "shipping_address_line")
    private String shippingAddressLine;

    @Size(max = 20)
    @Column(name = "shipping_ward_code", length = 20)
    private String shippingWardCode;

    @Size(max = 100)
    @Nationalized
    @Column(name = "shipping_ward_name", length = 100)
    private String shippingWardName;

    @Size(max = 20)
    @Column(name = "shipping_district_code", length = 20)
    private String shippingDistrictCode;

    @Size(max = 100)
    @Nationalized
    @Column(name = "shipping_district_name", length = 100)
    private String shippingDistrictName;

    @Size(max = 20)
    @Column(name = "shipping_province_code", length = 20)
    private String shippingProvinceCode;

    @Size(max = 100)
    @Nationalized
    @Column(name = "shipping_province_name", length = 100)
    private String shippingProvinceName;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "shipping_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal shippingFee;

    @Size(max = 100)
    @Nationalized
    @Column(name = "carrier_name", length = 100)
    private String carrierName;

    @Size(max = 100)
    @Nationalized
    @Column(name = "carrier_service_name", length = 100)
    private String carrierServiceName;

    @Size(max = 100)
    @Column(name = "carrier_rate_id", length = 100)
    private String carrierRateId;

    @Size(max = 255)
    @Nationalized
    @Column(name = "delivery_time_estimate")
    private String deliveryTimeEstimate;

    @Size(max = 100)
    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_method_id")
    private PaymentMethod paymentMethod;

    @Size(max = 80)
    @Nationalized
    @Column(name = "payment_method_name", length = 80)
    private String paymentMethodName;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'UNPAID'")
    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus;

    @Column(name = "payment_date")
    private OffsetDateTime paymentDate;

    @Size(max = 255)
    @Column(name = "transaction_id")
    private String transactionId;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discount;

    @NotNull
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Size(max = 50)
    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'PENDING'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "confirmed_at")
    private OffsetDateTime confirmedAt;

    @Column(name = "processing_at")
    private OffsetDateTime processingAt;

    @Column(name = "shipped_at")
    private OffsetDateTime shippedAt;

    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    @Column(name = "cancelled_at")
    private OffsetDateTime cancelledAt;

    @Column(name = "returned_at")
    private OffsetDateTime returnedAt;

    @OneToMany
    @JoinColumn(name = "order_id")
    @Builder.Default
    private Set<OrderDetail> orderDetails = new LinkedHashSet<>();

}

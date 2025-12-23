package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums.OrderStatus;
import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "order_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OrderHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private OrderStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_payment_status", length = 20)
    private PaymentStatus oldPaymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_payment_status", length = 20)
    private PaymentStatus newPaymentStatus;

    @Column(name = "changed_by_user_id")
    private Long changedByUserId;

    @Column(name = "note", length = 500, columnDefinition = "NVARCHAR(500)")
    private String note;
}

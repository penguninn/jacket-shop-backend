package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_histories", schema = "dbo", indexes = {
        @Index(name = "IX_order_histories_order", columnList = "order_id, created_at"),
        @Index(name = "IX_order_histories_user", columnList = "changed_by_user_id")
})
public class OrderHistory extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by_user_id")
    private User changedByUser;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 20)
    private Enums.OrderStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", length = 20)
    private Enums.OrderStatus newStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_payment_status", length = 20)
    private Enums.PaymentStatus oldPaymentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_payment_status", length = 20)
    private Enums.PaymentStatus newPaymentStatus;

    @Size(max = 1000)
    @Nationalized
    @Column(name = "note", length = 1000)
    private String note;

}

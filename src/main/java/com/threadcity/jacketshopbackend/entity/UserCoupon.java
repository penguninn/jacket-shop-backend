package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_coupons", schema = "dbo", indexes = {
        @Index(name = "IX_user_coupons_user", columnList = "user_id"),
        @Index(name = "IX_user_coupons_coupon", columnList = "coupon_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_user_coupons", columnNames = { "user_id", "coupon_id" })
})
public class UserCoupon extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @NotNull
    @Column(name = "used_count", nullable = false)
    @Builder.Default
    private Integer usedCount = 0;

    @Column(name = "first_used_at")
    private Instant firstUsedAt;

    @Column(name = "last_used_at")
    private Instant lastUsedAt;

}

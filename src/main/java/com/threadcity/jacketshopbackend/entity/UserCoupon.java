package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

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
    @ColumnDefault("0")
    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @Column(name = "first_used_at")
    private OffsetDateTime firstUsedAt;

    @Column(name = "last_used_at")
    private OffsetDateTime lastUsedAt;

}

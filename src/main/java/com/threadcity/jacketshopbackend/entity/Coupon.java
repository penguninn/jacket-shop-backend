package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "coupons", schema = "dbo", indexes = {
        @Index(name = "IX_coupons_code_status", columnList = "code, status"),
        @Index(name = "IX_coupons_dates", columnList = "valid_from, valid_to, status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_coupons_code", columnNames = { "code" })
})
public class Coupon extends BaseEntity {

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 20)
    @NotNull
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @NotNull
    @Column(name = "\"value\"", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "min_order_value", precision = 12, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "max_discount", precision = 12, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_limit_per_user")
    private Integer usageLimitPerUser;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "used_count", nullable = false)
    private Integer usedCount;

    @NotNull
    @Column(name = "valid_from", nullable = false)
    private OffsetDateTime validFrom;

    @NotNull
    @Column(name = "valid_to", nullable = false)
    private OffsetDateTime validTo;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

}

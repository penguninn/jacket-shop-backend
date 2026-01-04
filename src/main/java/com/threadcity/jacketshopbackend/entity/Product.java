package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products", schema = "dbo", indexes = {
        @Index(name = "IX_products_brand", columnList = "brand_id, status"),
        @Index(name = "IX_products_style", columnList = "style_id, status"),
        @Index(name = "IX_products_featured", columnList = "is_featured, status"),
        @Index(name = "IX_products_rating", columnList = "rating_average, rating_count"),
        @Index(name = "IX_products_sold", columnList = "sold_count")
})
public class Product extends BaseEntity {

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Size(max = 4000)
    @Nationalized
    @Column(name = "description", length = 4000)
    private String description;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_id", nullable = false)
    private Style style;

    @Column(name = "min_price", precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "sold_count", nullable = false)
    private Long soldCount;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "rating_count", nullable = false)
    private Integer ratingCount;

    @ColumnDefault("0")
    @Column(name = "rating_average", precision = 3, scale = 1)
    private BigDecimal ratingAverage;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "version", nullable = false)
    private Integer version;

}

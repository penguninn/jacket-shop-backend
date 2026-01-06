package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

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

    @Size(max = 500)
    @Column(name = "thumbnail", length = 500)
    private String thumbnail;

    @Column(name = "min_price", precision = 12, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 12, scale = 2)
    private BigDecimal maxPrice;

    @NotNull
    @Column(name = "is_featured", nullable = false)
    @Builder.Default
    private Boolean isFeatured = false;

    @NotNull
    @Column(name = "sold_count", nullable = false)
    @Builder.Default
    private Long soldCount = 0L;

    @NotNull
    @Column(name = "rating_count", nullable = false)
    @Builder.Default
    private Integer ratingCount = 0;

    @NotNull
    @Column(name = "rating_average", precision = 3, scale = 1)
    @Builder.Default
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;

    @NotNull
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Integer version = 1;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ProductVariant> variants = new LinkedHashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Review> reviews = new LinkedHashSet<>();

}

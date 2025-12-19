package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "style_id", nullable = false)
    private Style style;

    @Lob
    @Column(name = "thumbnail", columnDefinition = "NVARCHAR(MAX)")
    private String thumbnail;

    @Column(name = "is_featured")
    @Builder.Default
    private Boolean isFeatured = false;

    @Column(name = "sold_count")
    @Builder.Default
    private Long soldCount = 0L;

    @Column(name = "rating_average", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAverage = BigDecimal.ZERO;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    @Column(name = "min_price", precision = 19, scale = 2)
    private BigDecimal minPrice;

    @Column(name = "max_price", precision = 19, scale = 2)
    private BigDecimal maxPrice;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_colors", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "color_id"))
    @Builder.Default
    private Set<Color> colors = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_materials", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "material_id"))
    @Builder.Default
    private Set<Material> materials = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "product_sizes", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "size_id"))
    @Builder.Default
    private Set<Size> sizes = new HashSet<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();
}

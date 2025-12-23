package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_variants", uniqueConstraints = @UniqueConstraint(name = "uk_product_size_color_material", columnNames = {
        "product_id",
        "size_id",
        "color_id",
        "material_id"
}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {


    @Column(length = 255, nullable = false, unique = true)
    private String sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id", nullable = false)
    private Size size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @ManyToMany(mappedBy = "productVariants", fetch = FetchType.LAZY)
    @Builder.Default
    private List<Sale> sales = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    @Builder.Default
    @Column(name = "reserved_quantity")
    private Integer reservedQuantity = 0;

    @Builder.Default
    @Column(name = "available_quantity")
    private Integer availableQuantity = 0;

    @Builder.Default
    @Column(name = "sold_count")
    private Integer soldCount = 0;

    @Builder.Default
    @Column(name = "return_count")
    private Integer returnCount = 0;

    @Lob
    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String image;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(precision = 8, scale = 2)
    private BigDecimal length;

    @Column(precision = 8, scale = 2)
    private BigDecimal width;

    @Column(precision = 8, scale = 2)
    private BigDecimal height;
}

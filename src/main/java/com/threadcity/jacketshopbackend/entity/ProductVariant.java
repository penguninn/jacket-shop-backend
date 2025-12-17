package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "product_variants", uniqueConstraints = {
        @UniqueConstraint(name = "uk_product_size_color_material", columnNames = { "product_id", "size_id", "color_id",
                "material_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ProductVariant extends BaseEntity {

    @Column(length = 255, unique = true, nullable = false)
    private String sku;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "size_id")
    private Size size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "color_id")
    private Color color;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_id")
    private Material material;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id")
    private Sale sale;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false)
    @Builder.Default
    private Integer quantity = 0;

    @Lob
    @Column(name = "image", columnDefinition = "NVARCHAR(MAX)")
    private String image;

    @Column(name = "reserved_quantity")
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Column(name = "available_quantity")
    @Builder.Default
    private Integer availableQuantity = 0;

    @Column(precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(precision = 8, scale = 2)
    private BigDecimal length;

    @Column(precision = 8, scale = 2)
    private BigDecimal width;

    @Column(precision = 8, scale = 2)
    private BigDecimal height;

    @Column(name = "sold_count")
    @Builder.Default
    private Integer soldCount = 0;

    @Column(name = "return_count")
    @Builder.Default
    private Integer returnCount = 0;
}

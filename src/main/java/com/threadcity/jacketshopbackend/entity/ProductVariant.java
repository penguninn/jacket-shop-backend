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
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "product_variants", schema = "dbo", indexes = {
        @Index(name = "IX_product_variants_product", columnList = "product_id, status"),
        @Index(name = "IX_product_variants_sku", columnList = "sku"),
        @Index(name = "IX_product_variants_size", columnList = "size_id"),
        @Index(name = "IX_product_variants_color", columnList = "color_id"),
        @Index(name = "IX_product_variants_material", columnList = "material_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_product_variants_combination", columnNames = { "product_id", "size_id", "color_id",
                "material_id" }),
        @UniqueConstraint(name = "UK_product_variants_sku", columnNames = { "sku" })
})
public class ProductVariant extends BaseEntity {

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "size_id", nullable = false)
    private com.threadcity.jacketshopbackend.entity.Size size;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "color_id", nullable = false)
    private Color color;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", nullable = false)
    private Material material;

    @Size(max = 64)
    @NotNull
    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @NotNull
    @Column(name = "cost_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal costPrice;

    @NotNull
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "available_quantity", nullable = false)
    private Integer availableQuantity;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "reserved_quantity", nullable = false)
    private Integer reservedQuantity;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "sold_count", nullable = false)
    private Integer soldCount;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "return_count", nullable = false)
    private Integer returnCount;

    @Column(name = "weight", precision = 8, scale = 2)
    private BigDecimal weight;

    @Column(name = "length", precision = 8, scale = 2)
    private BigDecimal length;

    @Column(name = "width", precision = 8, scale = 2)
    private BigDecimal width;

    @Column(name = "height", precision = 8, scale = 2)
    private BigDecimal height;

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

package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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

import java.math.BigDecimal;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_details", schema = "dbo", indexes = {
        @Index(name = "IX_order_details_order", columnList = "order_id"),
        @Index(name = "IX_order_details_variant", columnList = "product_variant_id")
})
public class OrderDetail extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Size(max = 200)
    @NotNull
    @Nationalized
    @Column(name = "product_name", nullable = false, length = 200)
    private String productName;

    @Size(max = 64)
    @NotNull
    @Column(name = "sku", nullable = false, length = 64)
    private String sku;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "color", nullable = false, length = 50)
    private String color;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "\"size\"", nullable = false, length = 50)
    private String size;

    @Size(max = 50)
    @NotNull
    @Nationalized
    @Column(name = "material", nullable = false, length = 50)
    private String material;

    @Size(max = 500)
    @Nationalized
    @Column(name = "image", length = 500)
    private String image;

    @NotNull
    @Column(name = "original_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal originalPrice;

    @NotNull
    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @NotNull
    @Min(1)
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

}

package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
@Table(name = "cart_items", schema = "dbo", indexes = {
        @Index(name = "IX_cart_items_cart", columnList = "cart_id"),
        @Index(name = "IX_cart_items_variant", columnList = "product_variant_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_cart_items_variant", columnNames = { "cart_id", "product_variant_id" })
})
public class CartItem extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @NotNull
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @NotNull
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "selected", nullable = false)
    @Builder.Default
    private Boolean selected = false;

}

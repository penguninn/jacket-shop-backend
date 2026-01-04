package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sale_variants", schema = "dbo", indexes = {
        @Index(name = "IX_sale_variants_variant", columnList = "product_variant_id")
})
public class SaleVariant {
    @EmbeddedId
    private SaleVariantId id;

    @MapsId("saleId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @MapsId("productVariantId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @NotNull
    @ColumnDefault("sysdatetimeoffset()")
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

}

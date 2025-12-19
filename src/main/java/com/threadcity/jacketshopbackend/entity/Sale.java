package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Sale extends BaseEntity {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "sale_product_variants",
        joinColumns = @JoinColumn(name = "sale_id"),
        inverseJoinColumns = @JoinColumn(name = "product_variant_id")
    )
    private List<ProductVariant> productVariants;

    @Column(name = "name", length = 255, columnDefinition = "NVARCHAR(255)")
    private String name;

    @Column(name = "description", length = 255, columnDefinition = "NVARCHAR(255)")
    private String description;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;
}

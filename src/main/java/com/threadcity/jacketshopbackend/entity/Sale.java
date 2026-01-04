package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sales", schema = "dbo", indexes = {
        @Index(name = "IX_sales_dates_status", columnList = "start_date, end_date, status")
})
public class Sale extends BaseEntity {

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Column(name = "discount_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @NotNull
    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @NotNull
    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

}

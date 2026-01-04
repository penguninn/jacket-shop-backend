package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "payment_methods", schema = "dbo", indexes = {
        @Index(name = "IX_payment_methods_status_type", columnList = "status, type")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_payment_methods_code", columnNames = { "code" })
})
public class PaymentMethod extends BaseEntity {

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Size(max = 50)
    @NotNull
    @Column(name = "code", nullable = false, length = 50)
    private String code;

    @Size(max = 100)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 20)
    @NotNull
    @Column(name = "type", nullable = false, length = 20)
    private Enums.PaymentMethodType type;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private Enums.Status status;

}

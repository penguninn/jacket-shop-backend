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
import org.hibernate.annotations.Nationalized;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "brands", schema = "dbo", indexes = {
        @Index(name = "IX_brands_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_brands_name", columnNames = { "name" })
})
public class Brand extends BaseEntity {

    @Size(max = 120)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 500)
    @Column(name = "logo", length = 500)
    private String logo;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

}

package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "sizes", schema = "dbo", indexes = {
        @Index(name = "IX_sizes_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_sizes_name", columnNames = { "name" })
})
public class Size extends BaseEntity {

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @jakarta.validation.constraints.Size(max = 120)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @jakarta.validation.constraints.Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @jakarta.validation.constraints.Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

}

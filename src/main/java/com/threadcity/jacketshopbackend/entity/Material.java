package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "materials", schema = "dbo", indexes = {
        @Index(name = "IX_materials_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_materials_name", columnNames = { "name" })
})
public class Material extends BaseEntity {

    @Size(max = 120)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Enums.Status status = Enums.Status.ACTIVE;

}

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

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "colors", schema = "dbo", indexes = {
        @Index(name = "IX_colors_status", columnList = "status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_colors_name", columnNames = { "name" }),
        @UniqueConstraint(name = "UK_colors_hex_code", columnNames = { "hex_code" })
})
public class Color extends BaseEntity {

    @Size(max = 120)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Size(max = 10)
    @NotNull
    @Column(name = "hex_code", nullable = false, length = 10)
    private String hexCode;

    @Size(max = 500)
    @Nationalized
    @Column(name = "description", length = 500)
    private String description;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ACTIVE'")
    @Column(name = "status", nullable = false, length = 20)
    private String status;

}

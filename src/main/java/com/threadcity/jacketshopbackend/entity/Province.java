package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "provinces", schema = "dbo", indexes = {
        @Index(name = "IX_provinces_code", columnList = "code")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_provinces_name", columnNames = { "name" })
})
public class Province extends BaseEntity {

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false)
    private String name;

    @Size(max = 50)
    @Column(name = "goship_id", length = 50)
    private String goshipId;

    @Size(max = 20)
    @Column(name = "code", length = 20)
    private String code;

}

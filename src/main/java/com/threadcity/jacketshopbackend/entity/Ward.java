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
@Table(name = "wards", schema = "dbo", indexes = {
        @Index(name = "IX_wards_district", columnList = "district_id"),
        @Index(name = "IX_wards_code", columnList = "code")
})
public class Ward extends BaseEntity {

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Size(max = 50)
    @Column(name = "goship_id", length = 50)
    private String goshipId;

    @Size(max = 20)
    @Column(name = "code", length = 20)
    private String code;

}

package com.threadcity.jacketshopbackend.entity;

import java.util.List;
import java.util.ArrayList;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Province {

    @Id
    private Integer code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 50)
    private String codename;

    @Column(name = "division_type", length = 50)
    private String divisionType;

    @Column(name = "phone_code")
    private Integer phoneCode;

    @OneToMany(mappedBy = "province")
    @Builder.Default
    private List<Ward> wards = new ArrayList<>();
}

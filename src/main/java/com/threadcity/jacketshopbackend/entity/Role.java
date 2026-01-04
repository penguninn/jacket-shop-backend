package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "roles", schema = "dbo", uniqueConstraints = {
        @UniqueConstraint(name = "UK_roles_name", columnNames = { "name" })
})
public class Role extends BaseEntity {

    @Size(max = 50)
    @NotNull
    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Size(max = 255)
    @Nationalized
    @Column(name = "description")
    private String description;

}

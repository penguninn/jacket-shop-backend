package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "addresses", schema = "dbo", indexes = {
        @Index(name = "IX_addresses_user", columnList = "user_id"),
        @Index(name = "IX_addresses_user_default", columnList = "user_id, is_default")
})
public class Address extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 120)
    @NotNull
    @Nationalized
    @Column(name = "recipient_name", nullable = false, length = 120)
    private String recipientName;

    @Size(max = 15)
    @NotNull
    @Column(name = "recipient_phone", nullable = false, length = 15)
    private String recipientPhone;

    @Size(max = 255)
    @NotNull
    @Nationalized
    @Column(name = "address_line", nullable = false)
    private String addressLine;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ward_id", nullable = false)
    private Ward ward;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "province_id", nullable = false)
    private Province province;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private Boolean isDefault = false;

}

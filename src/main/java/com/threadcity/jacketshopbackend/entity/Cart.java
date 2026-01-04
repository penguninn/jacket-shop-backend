package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "carts", schema = "dbo", uniqueConstraints = {
        @UniqueConstraint(name = "UK_carts_user", columnNames = { "user_id" })
})
public class Cart extends BaseEntity {

    @NotNull
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany
    @JoinColumn(name = "cart_id")
    @Builder.Default
    private Set<CartItem> cartItems = new LinkedHashSet<>();

}

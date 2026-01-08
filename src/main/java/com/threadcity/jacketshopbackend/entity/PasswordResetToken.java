package com.threadcity.jacketshopbackend.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "password_reset_tokens", schema = "dbo", indexes = {
        @Index(name = "IX_password_reset_tokens_user", columnList = "user_id"),
        @Index(name = "IX_password_reset_tokens_expiry", columnList = "expiry_date, is_used")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_password_reset_tokens_token", columnNames = { "token" })
})
public class PasswordResetToken extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 255)
    @NotNull
    @Column(name = "token", nullable = false)
    private String token;

    @NotNull
    @Column(name = "expiry_date", nullable = false)
    private Instant expiryDate;

    @NotNull
    @Column(name = "is_used", nullable = false)
    @Builder.Default
    private Boolean isUsed = false;

}

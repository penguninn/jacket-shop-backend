package com.threadcity.jacketshopbackend.entity;

import com.threadcity.jacketshopbackend.common.Enums;
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
@Table(name = "refresh_tokens", schema = "dbo", indexes = {
        @Index(name = "IX_refresh_tokens_user", columnList = "user_id"),
        @Index(name = "IX_refresh_tokens_expires", columnList = "expires_at, status")
}, uniqueConstraints = {
        @UniqueConstraint(name = "UK_refresh_tokens_jti", columnNames = { "jti" })
})
public class RefreshToken extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Size(max = 64)
    @NotNull
    @Column(name = "jti", nullable = false, length = 64)
    private String jti;

    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private Enums.RefreshTokenStatus status = Enums.RefreshTokenStatus.ACTIVE;

}

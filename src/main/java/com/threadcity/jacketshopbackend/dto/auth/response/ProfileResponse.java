package com.threadcity.jacketshopbackend.dto.auth.response;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
public class ProfileResponse implements Serializable {

    private Long id;

    private String username;

    private String email;

    private String fullName;

    private String phone;

    private String avatar;

    private Set<RoleResponse> roles;

    private Status status;

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private Instant lastLoginAt;

    private Instant createdAt;

    private Instant updatedAt;

}

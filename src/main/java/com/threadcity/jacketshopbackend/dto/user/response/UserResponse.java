package com.threadcity.jacketshopbackend.dto.user.response;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.dto.auth.response.RoleResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse implements Serializable {

    private Long id;

    private String fullName;

    private String username;

    private String email;

    private String phone;

    private String avatar;

    private Status status;

    private Boolean emailVerified;

    private Boolean phoneVerified;

    private Instant lastLoginAt;

    private Instant createdAt;

    private Instant updatedAt;

    private List<RoleResponse> roles;
}

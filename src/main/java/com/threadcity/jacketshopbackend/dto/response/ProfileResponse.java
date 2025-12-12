package com.threadcity.jacketshopbackend.dto.response;

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

    private String fullName;

    private String phone;

    private Set<RoleResponse> roles;

    private Status status;

    private Instant createdAt;

    private Instant updatedAt;

}

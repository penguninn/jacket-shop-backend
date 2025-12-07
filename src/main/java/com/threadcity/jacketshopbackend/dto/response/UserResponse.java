package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.Set;

import com.threadcity.jacketshopbackend.common.Enums.Status;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse implements Serializable {

    private Long id;

    private String fullName;

    private String username;

    private String phone;

    private Status status;

    private Instant createdAt;

    private Instant updatedAt;

    private Set<String> roles;
}

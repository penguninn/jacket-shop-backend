package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.entity.Address;
import com.threadcity.jacketshopbackend.entity.Role;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
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

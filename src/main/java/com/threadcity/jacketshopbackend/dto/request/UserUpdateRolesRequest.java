package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

@Data
@Builder
public class UserUpdateRolesRequest implements Serializable {

    @NotNull(message = "Role IDs cannot be null")
    @NotEmpty(message = "Role IDs cannot be empty")
    private Set<Long> roleIds;
}

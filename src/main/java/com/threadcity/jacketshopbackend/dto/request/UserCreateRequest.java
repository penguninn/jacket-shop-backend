package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import com.threadcity.jacketshopbackend.entity.Address;
import com.threadcity.jacketshopbackend.entity.Role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserCreateRequest implements Serializable {

    @NotBlank
    @Size(min = 2, max = 120)
    private String fullName;

    @NotBlank
    @Size(min = 6, max = 50)
    private String username;

    @NotBlank
    @Size(min = 8, max = 128)
    private String password;

    @Pattern(regexp = "^0\\d{9,10}$")
    private String phoneNumber;
}

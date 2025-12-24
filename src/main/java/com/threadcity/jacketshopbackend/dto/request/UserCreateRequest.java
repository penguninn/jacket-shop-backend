package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums.Status;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class UserCreateRequest implements Serializable {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 6, max = 50, message = "Username must be between 6 and 50 characters")
    private String username;

    @NotNull(message = "Status is required")
    private Status status;

    @NotNull(message = "Role IDs are required")
    @NotEmpty(message = "At least one role must be selected")
    @Size(min = 1, message = "At least one role must be selected")
    private List<Long> roleIds;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 128, message = "Password must be between 8 and 128 characters")
    private String password;

    @Pattern(regexp = "^0\\d{9,14}$", message = "Phone number must start with 0 and have 10-15 digits")
    private String phone;
}

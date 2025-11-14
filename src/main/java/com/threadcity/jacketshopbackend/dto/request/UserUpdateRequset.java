package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import com.threadcity.jacketshopbackend.common.Enums.Status;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequset implements Serializable {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 120, message = "Full name must be between 2 and 120 characters")
    private String fullName;

    @NotNull(message = "Status is required")
    private Status status;

    @NotNull(message = "Role IDs are required")
    @NotEmpty(message = "At least one role must be selected")
    @Size(min = 1, message = "At least one role must be selected")
    private List<Long> roleIds;

    @Pattern(regexp = "^0\\d{9,15}$", message = "Phone number must start with 0 and have 10-15 digits")
    private String phone;
}

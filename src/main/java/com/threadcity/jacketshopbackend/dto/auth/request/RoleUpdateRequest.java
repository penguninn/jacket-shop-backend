package com.threadcity.jacketshopbackend.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoleUpdateRequest {

    @NotNull(message = "ID is required")
    private Long id;

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;
}

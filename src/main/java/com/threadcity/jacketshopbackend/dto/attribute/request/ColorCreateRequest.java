package com.threadcity.jacketshopbackend.dto.attribute.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ColorCreateRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 120, message = "Name must be less than 120 characters")
    private String name;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotBlank(message = "Hex code cannot be empty")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "Hex code must be in format #RRGGBB")
    @Size(max = 10, message = "Hex code must be less than 10 characters")
    private String hexCode;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

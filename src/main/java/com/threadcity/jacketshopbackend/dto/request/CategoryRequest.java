package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Name cannot be empty")
    @Size(max = 120, message = "Name must be less than 120 characters")
    private String name;

    @NotNull(message = "Status is required")
    private Enums.Status status;

    @Size(max = 255, message = "Description too long")
    private String description;
}

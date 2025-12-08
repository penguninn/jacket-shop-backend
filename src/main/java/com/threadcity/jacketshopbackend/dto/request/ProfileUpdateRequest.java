package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProfileUpdateRequest implements Serializable {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name must be less than 150 characters")
    private String fullName;
}

package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor         // constructor không tham số
@AllArgsConstructor        // constructor tất cả tham số (public)
public class MaterialRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 120, message = "Name must be less than 120 characters")
    private String name;

    @Size(max = 255, message = "Description too long")
    private String description;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

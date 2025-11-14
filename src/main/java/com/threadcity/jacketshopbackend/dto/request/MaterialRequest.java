package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class MaterialRequest {
    @NotNull(message = "Name cannot be null")

    private String name; // da, jean, kaki, du, etc.

    private String description;

    private Enums.Status status;


}

package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MaterialStatusRequest {

    @NotNull(message = "Status cannot be null")
    private Enums.Status status;
}

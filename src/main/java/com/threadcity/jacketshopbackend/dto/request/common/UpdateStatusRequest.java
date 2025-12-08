package com.threadcity.jacketshopbackend.dto.request.common;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class UpdateStatusRequest implements Serializable {

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

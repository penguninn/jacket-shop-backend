package com.threadcity.jacketshopbackend.dto.request.common;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkStatusRequest {
    @NotNull(message = "List of IDs cannot be null")
    @NotEmpty(message = "List of IDs cannot be empty")
    private List<Long> ids;

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

package com.threadcity.jacketshopbackend.dto.common.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkDeleteRequest {
    @NotNull(message = "List of IDs cannot be null")
    @NotEmpty(message = "List of IDs cannot be empty")
    private List<Long> ids;
}

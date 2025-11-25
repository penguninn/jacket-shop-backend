package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;
import java.util.List;

import com.threadcity.jacketshopbackend.common.Enums.Status;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserBulkStatusRequest implements Serializable {

    @NotEmpty
    private List<Long> ids;

    @NotNull
    private Status status;
}

package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;
import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserBulkDeleteRequest implements Serializable {

    @NotEmpty
    private List<Long> ids;
}

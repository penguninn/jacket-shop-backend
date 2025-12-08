package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
public class UserBulkDeleteRequest implements Serializable {

    @NotEmpty
    private List<Long> ids;
}

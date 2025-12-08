package com.threadcity.jacketshopbackend.dto.request.common;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkStatusRequest {
    private List<Long> ids;
    private Enums.Status status;
}

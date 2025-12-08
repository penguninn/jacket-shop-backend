package com.threadcity.jacketshopbackend.dto.request.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkDeleteRequest {
    private List<Long> ids;
}

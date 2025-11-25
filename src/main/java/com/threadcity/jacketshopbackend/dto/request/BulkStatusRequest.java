package com.threadcity.jacketshopbackend.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BulkStatusRequest {
    private List<Long> ids;
    private String status;
}

package com.threadcity.jacketshopbackend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
public class ApiResponse<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private Instant timestamp;
}

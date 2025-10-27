package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;
import java.time.Instant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiResponse<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private Instant timestamp;
}

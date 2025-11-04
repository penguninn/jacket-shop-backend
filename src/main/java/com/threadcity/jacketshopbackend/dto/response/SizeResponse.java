package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;

import java.io.Serializable;
import java.time.Instant;

public class SizeResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

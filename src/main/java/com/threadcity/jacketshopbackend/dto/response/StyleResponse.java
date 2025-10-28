package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;

import java.io.Serializable;
import java.time.Instant;

public class StyleResponse implements Serializable {
    private Integer id;
    private String name; // bomber, biker, hoodie, blazer, etc.
    private String description;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

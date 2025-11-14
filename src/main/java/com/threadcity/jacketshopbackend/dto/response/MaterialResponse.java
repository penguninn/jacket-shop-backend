package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;

import java.io.Serializable;
import java.time.Instant;

public class MaterialResponse implements Serializable {
    private String name; // da, jean, kaki, du, etc.
    private String description;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

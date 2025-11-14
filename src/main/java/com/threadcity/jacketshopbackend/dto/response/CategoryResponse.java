package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
public class CategoryResponse implements Serializable{
    private Long id;
    private String name;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;
}

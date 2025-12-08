package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse implements Serializable {
    private Long id;
    private String name;
    private Enums.Status status;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}

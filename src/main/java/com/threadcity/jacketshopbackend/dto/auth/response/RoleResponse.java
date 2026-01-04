package com.threadcity.jacketshopbackend.dto.auth.response;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleResponse implements Serializable {

    private Long id;

    private String name;

    private String description;

    private Instant createdAt;

    private Instant updatedAt;
}

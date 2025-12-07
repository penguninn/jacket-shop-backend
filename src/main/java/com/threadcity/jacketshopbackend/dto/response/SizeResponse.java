package com.threadcity.jacketshopbackend.dto.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SizeResponse implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Enums.Status status;
    private Instant createdAt;
    private Instant updatedAt;

}

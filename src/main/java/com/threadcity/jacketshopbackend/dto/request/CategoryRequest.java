package com.threadcity.jacketshopbackend.dto.request;


import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Data
public class CategoryRequest {
    @NotNull(message = "Name cannot be null")
    private String name;
    private Enums.Status status;
}

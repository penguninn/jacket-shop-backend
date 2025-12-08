package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class RefreshTokenRequest implements Serializable {

    @NotBlank(message = "Token cannot be empty")
    private String token;
}

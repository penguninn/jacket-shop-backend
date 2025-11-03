package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RefreshTokenRequest implements Serializable {

    @NotBlank
    private String token;   
}

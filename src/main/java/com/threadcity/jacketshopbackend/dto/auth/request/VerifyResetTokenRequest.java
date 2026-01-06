package com.threadcity.jacketshopbackend.dto.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerifyResetTokenRequest implements Serializable {

    @NotBlank(message = "Token is required")
    private String token;
}

package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse implements Serializable {

    private String accessToken;

    private String refreshToken;
}

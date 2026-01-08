package com.threadcity.jacketshopbackend.dto.auth.response;

import lombok.Builder;
import lombok.Data;

import com.threadcity.jacketshopbackend.dto.user.response.UserResponse;

import java.io.Serializable;

@Data
@Builder
public class LoginResponse implements Serializable {

    private String accessToken;

    private String refreshToken;

    private UserResponse user;
}

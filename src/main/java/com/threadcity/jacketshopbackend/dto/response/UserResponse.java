package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResponse implements Serializable {

    private Long id;
    private String fullName;
    private List<String> roles;
}

package com.threadcity.jacketshopbackend.dto.response;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileResponse implements Serializable {

    private Long id;

    private String username;

    private String fullName;

    private String phone;

    private Set<String> roles;
    
    private Instant createdAt;

    private Instant updatedAt;

}

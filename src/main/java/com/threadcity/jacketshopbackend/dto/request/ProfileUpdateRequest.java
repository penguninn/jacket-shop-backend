package com.threadcity.jacketshopbackend.dto.request;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ProfileUpdateRequest implements Serializable {

    private String fullName;
}

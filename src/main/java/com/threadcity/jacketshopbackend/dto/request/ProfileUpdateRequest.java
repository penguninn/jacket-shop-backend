package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProfileUpdateRequest implements Serializable {

    private String fullName;
}

package com.threadcity.jacketshopbackend.dto.request;

import java.io.Serializable;
import java.util.Set;

import com.threadcity.jacketshopbackend.common.Enums.Status;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserUpdateRequset implements Serializable {

    @Size(min = 2, max = 120)
    private String fullName;

    @Pattern(regexp = "^0\\d{9,10}$")
    private String phoneNumber;
}

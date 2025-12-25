package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 50, message = "Code must be less than 50 characters")
    private String code;

    @NotNull(message = "Type is required")
    private Enums.PaymentMethodType type;

    @Size(max = 255, message = "Description too long")
    private String description;

    private String config; // JSON string

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

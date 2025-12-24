package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class PaymentMethodRequest {

    @NotBlank(message = "Name cannot be empty")
    @Size(max = 100, message = "Name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Code cannot be empty")
    @Size(max = 50, message = "Code must be less than 50 characters")
    private String code;

    @NotEmpty(message = "At least one type is required")
    private List<Enums.PaymentMethodType> types;

    @Size(max = 255, message = "Description too long")
    private String description;

    private String config; // JSON string

    @NotNull(message = "Status is required")
    private Enums.Status status;
}

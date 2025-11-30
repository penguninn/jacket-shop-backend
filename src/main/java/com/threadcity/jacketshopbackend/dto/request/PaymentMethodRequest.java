package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    @NotNull(message = "Name cannot be null")
    private String name;

    private String description;

    private String configJson;

    private Enums.Status status;
}

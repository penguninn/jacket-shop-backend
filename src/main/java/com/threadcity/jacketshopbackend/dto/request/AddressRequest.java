package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class AddressRequest implements Serializable {

    @NotBlank(message = "Address line cannot be empty")
    @Size(max = 255, message = "Address line must be less than 255 characters")
    private String addressLine;

    @NotNull(message = "Ward ID is required")
    private Long wardId;

    @NotNull(message = "District ID is required")
    private Long districtId;

    @NotNull(message = "Province ID is required")
    private Long provinceId;

    private Boolean isDefault;

    @Size(max = 120, message = "Recipient name must be less than 120 characters")
    private String recipientName;

    @Size(min = 10, max = 20, message = "Phone number must be between 10 and 20 characters")
    @Pattern(regexp = "^0\\d{9,14}$", message = "Phone number must start with 0 and contain only digits")
    private String recipientPhone;
}

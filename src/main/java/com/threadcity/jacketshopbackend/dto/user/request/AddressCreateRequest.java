package com.threadcity.jacketshopbackend.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class AddressCreateRequest implements Serializable {

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

    @NotBlank(message = "Recipient name cannot be empty")
    @Size(max = 120, message = "Recipient name must be less than 120 characters")
    private String recipientName;

    @NotBlank(message = "Recipient phone cannot be empty")
    @Size(max = 15, message = "Recipient phone must be at most 15 characters")
    @Pattern(regexp = "^0\\d{9,14}$", message = "Phone number must start with 0 and have 10-15 digits")
    private String recipientPhone;
}

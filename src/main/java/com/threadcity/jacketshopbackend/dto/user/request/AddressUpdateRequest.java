package com.threadcity.jacketshopbackend.dto.user.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressUpdateRequest {

    @NotNull(message = "ID is required")
    private Long id;

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotBlank(message = "Recipient name cannot be empty")
    @Size(max = 200, message = "Recipient name must be less than 200 characters")
    private String recipientName;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
    private String phoneNumber;

    @NotBlank(message = "Address line cannot be empty")
    @Size(max = 500, message = "Address line must be less than 500 characters")
    private String addressLine;

    @NotNull(message = "Province ID is required")
    private Long provinceId;

    @NotNull(message = "District ID is required")
    private Long districtId;

    @NotNull(message = "Ward ID is required")
    private Long wardId;

    @Builder.Default
    private Boolean isDefault = false;
}

package com.threadcity.jacketshopbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {

    @NotBlank(message = "Customer name is required")
    private String customerName;

    private String customerEmail;

    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

    @NotBlank(message = "Shipping recipient name is required")
    private String shippingRecipientName;

    @NotBlank(message = "Shipping recipient phone is required")
    private String shippingRecipientPhone;

    @NotBlank(message = "Shipping address line is required")
    private String shippingAddressLine;

    private String shippingProvinceCode;
    private String shippingDistrictCode;
    private String shippingWardCode;

    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;

    @NotNull(message = "Shipping method ID is required")
    private Long shippingMethodId;

    private String couponCode;
    private String note;
}

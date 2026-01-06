package com.threadcity.jacketshopbackend.dto.order.request;

import com.threadcity.jacketshopbackend.common.Enums.OrderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderRequest implements Serializable {

    @NotNull(message = "Order Type is required")
    private OrderType orderType;

    private Long paymentMethodId;

    private String note;

    private String couponCode;

    private List<OrderItemRequest> items;

    private Long userId;

    @NotBlank(message = "Customer name is required")
    @Size(max = 120, message = "Customer name must be less than 120 characters")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    @Size(max = 15, message = "Customer phone must be at most 15 characters")
    @Pattern(regexp = "^0\\d{9,14}$", message = "Phone number must start with 0 and contain only digits")
    private String customerPhone;

    @Email(message = "Email must be valid")
    @Size(max = 255, message = "Email must be less than 255 characters")
    private String customerEmail;

    private Long addressId;

    @Size(max = 120, message = "Shipping recipient name must be less than 120 characters")
    private String shippingRecipientName;

    @Size(max = 15, message = "Shipping recipient phone must be at most 15 characters")
    private String shippingRecipientPhone;

    @Size(max = 255, message = "Shipping address line must be less than 255 characters")
    private String shippingAddressLine;

    @Size(max = 20, message = "Shipping ward code must be at most 20 characters")
    private String shippingWardCode;

    @Size(max = 100, message = "Shipping ward name must be less than 100 characters")
    private String shippingWardName;

    @Size(max = 20, message = "Shipping district code must be at most 20 characters")
    private String shippingDistrictCode;

    @Size(max = 100, message = "Shipping district name must be less than 100 characters")
    private String shippingDistrictName;

    @Size(max = 20, message = "Shipping province code must be at most 20 characters")
    private String shippingProvinceCode;

    @Size(max = 100, message = "Shipping province name must be less than 100 characters")
    private String shippingProvinceName;

    private BigDecimal shippingFee;

    @Size(max = 100, message = "Carrier name must be less than 100 characters")
    private String carrierName;

    @Size(max = 100, message = "Carrier service name must be less than 100 characters")
    private String carrierServiceName;

    @Size(max = 100, message = "Carrier rate ID must be at most 100 characters")
    private String carrierRateId;

    @Size(max = 255, message = "Delivery time estimate must be less than 255 characters")
    private String deliveryTimeEstimate;

    @Size(max = 100, message = "Tracking number must be at most 100 characters")
    private String trackingNumber;

    @Size(max = 255, message = "Transaction ID must be less than 255 characters")
    private String transactionId;

}

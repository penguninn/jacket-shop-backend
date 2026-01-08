package com.threadcity.jacketshopbackend.dto.order.request;

import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class UpdatePaymentRequest implements Serializable {
    @NotNull(message = "Payment method ID is required")
    private Long paymentMethodId;

    @NotNull(message = "Payment status is required")
    private PaymentStatus paymentStatus;

    private String transactionId;
}

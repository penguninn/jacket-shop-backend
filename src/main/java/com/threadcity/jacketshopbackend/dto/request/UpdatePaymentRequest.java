package com.threadcity.jacketshopbackend.dto.request;

import com.threadcity.jacketshopbackend.common.Enums.PaymentStatus;
import lombok.Data;

@Data
public class UpdatePaymentRequest {
    private Long paymentMethodId;
    private PaymentStatus paymentStatus;
}

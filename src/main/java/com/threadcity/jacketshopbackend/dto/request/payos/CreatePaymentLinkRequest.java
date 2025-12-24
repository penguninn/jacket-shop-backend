package com.threadcity.jacketshopbackend.dto.request.payos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreatePaymentLinkRequest {
    private String productName;
    private String description;
    private String returnUrl;
    private Integer price;
    private String cancelUrl;
}

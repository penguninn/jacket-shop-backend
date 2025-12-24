package com.threadcity.jacketshopbackend.dto.request.payos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmWebhookRequest {
    private String webhookUrl;
}

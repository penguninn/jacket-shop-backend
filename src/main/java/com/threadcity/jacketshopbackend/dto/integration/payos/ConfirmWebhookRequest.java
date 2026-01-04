package com.threadcity.jacketshopbackend.dto.integration.payos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfirmWebhookRequest {
    private String webhookUrl;
}

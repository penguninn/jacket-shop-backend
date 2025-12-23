package com.threadcity.jacketshopbackend.service;

import com.threadcity.jacketshopbackend.dto.goship.GoshipResponse;
import com.threadcity.jacketshopbackend.dto.goship.rate.GoshipRateData;
import com.threadcity.jacketshopbackend.dto.goship.rate.GoshipRateRequest;
import com.threadcity.jacketshopbackend.exception.ErrorCodes;
import com.threadcity.jacketshopbackend.exception.ExternalServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShippingService {

    @Value("${goship.url}")
    private String goshipUrl;

    @Value("${goship.token}")
    private String goshipToken;

    private RestClient getRestClient() {
        return RestClient.builder()
                .baseUrl(goshipUrl)
                .defaultHeader("Authorization", "Bearer " + goshipToken)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public List<GoshipRateData> getRates(GoshipRateRequest request) {
        log.info("Fetching shipping rates from Goship");
        try {
            GoshipResponse<GoshipRateData> response = getRestClient().post()
                    .uri("/rates")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response != null && "success".equalsIgnoreCase(response.getStatus())) {
                return response.getData();
            } else {
                log.error("Goship returned error or empty response: {}", response);
                throw new ExternalServiceException("Goship", ErrorCodes.GOSHIP_API_ERROR, "Failed to fetch rates from Goship", null);
            }

        } catch (Exception e) {
            log.error("Error calling Goship API: {}", e.getMessage(), e);
            throw new ExternalServiceException("Goship", ErrorCodes.GOSHIP_API_ERROR, "Error communicating with shipping service: " + e.getMessage(), e);
        }
    }
}

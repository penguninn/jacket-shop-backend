package com.threadcity.jacketshopbackend.dto.goship.rate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GoshipRateData {

    private String id;
    
    @JsonProperty("carrier_name")
    private String carrierName;
    
    @JsonProperty("carrier_logo")
    private String carrierLogo;
    
    private String service;
    
    private String expected;
    
    @JsonProperty("cod_fee")
    private Long codFee;
    
    @JsonProperty("total_fee")
    private Long totalFee;
    
    @JsonProperty("total_amount")
    private Long totalAmount;
    
    @JsonProperty("expected_txt")
    private String expectedTxt;
}

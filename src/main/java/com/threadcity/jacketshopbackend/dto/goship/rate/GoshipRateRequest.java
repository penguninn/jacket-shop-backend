package com.threadcity.jacketshopbackend.dto.goship.rate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GoshipRateRequest {
    private Shipment shipment;

    @Data
    @Builder
    public static class Shipment {
        @JsonProperty("address_from")
        private AddressDto addressFrom;

        @JsonProperty("address_to")
        private AddressDto addressTo;

        private Parcel parcel;
    }

    @Data
    @Builder
    public static class AddressDto {
        private String district;
        private String city;
    }

    @Data
    @Builder
    public static class Parcel {
        private Long cod;
        private Long amount;
        private Integer width;
        private Integer height;
        private Integer length;
        private Integer weight;
    }
}

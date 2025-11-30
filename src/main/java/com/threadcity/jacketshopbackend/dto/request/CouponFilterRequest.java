package com.threadcity.jacketshopbackend.dto.request;


import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponFilterRequest {

    @Size(max = 255, message = "Search term too long")
    private String search; // search in code or description

    private List<String> status; // ACTIVE, INACTIVE
    private List<String> type;   // FIXED, PERCENTAGE

    private Instant validFrom; // filter coupons starting from date
    private Instant validTo;   // filter coupons ending before date

    @Builder.Default
    @Min(value = 0, message = "Page must be >= 0")
    private int page = 0;

    @Builder.Default
    @Min(value = 1, message = "Size must be >= 1")
    @Max(value = 100, message = "Size must be <= 100")
    private int size = 10;

    @Builder.Default
    @Pattern(
            regexp = "^(id|code|validFrom|validTo|createdAt|updatedAt)$",
            message = "Invalid sort field"
    )
    private String sortBy = "createdAt";

    @Builder.Default
    @Pattern(
            regexp = "^(ASC|DESC)$",
            message = "Sort direction must be ASC or DESC"
    )
    private String sortDir = "DESC";
}

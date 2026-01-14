package com.threadcity.jacketshopbackend.dto.statistics.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerStatisticsResponse {

    private Long totalCustomers;

    private Long newCustomersThisMonth;

    private Long activeCustomers;
}

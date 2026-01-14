package com.threadcity.jacketshopbackend.dto.statistics.response;

import com.threadcity.jacketshopbackend.common.Enums;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderStatusStatisticsResponse {

    private Enums.OrderStatus status;

    private Long count;
}

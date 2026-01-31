package com.logistics.hub.feature.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatisticsResponse {
    private long total;
    private long pending;
    private long inTransit;
}

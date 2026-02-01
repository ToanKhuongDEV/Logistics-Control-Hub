package com.logistics.hub.feature.driver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverStatisticsResponse {
    private long total;
    private long available;
    private long assigned;
}

package com.logistics.hub.feature.dashboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatisticsResponse {

  private Long activeVehicles;
  private Long totalOrders;
  private Long ordersCreated;
  private Long ordersInTransit;
  private Long ordersDelivered;
  private Long ordersCancelled;
  private Long activeDepots;
  private Long totalDrivers;
  private Long totalRoutingRuns;
  private Long successfulRuns;
}

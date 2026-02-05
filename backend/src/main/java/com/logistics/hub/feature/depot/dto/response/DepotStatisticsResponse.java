package com.logistics.hub.feature.depot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepotStatisticsResponse {
  private long total;
  private long active;
  private long inactive;
}

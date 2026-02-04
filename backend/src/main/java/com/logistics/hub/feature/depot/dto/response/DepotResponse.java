package com.logistics.hub.feature.depot.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DepotResponse {
  private Long id;
  private String name;
  private Long locationId;
  private String address;
  private String description;
  private LocalDateTime createdAt;
}

package com.logistics.hub.feature.depot.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DepotResponse {
  private Long id;
  private String name;
  private Long locationId;
  private String street;
  private String city;
  private String country;
  private String description;
  private Boolean isActive;
  private LocalDateTime createdAt;
}

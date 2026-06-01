package com.logistics.hub.feature.depot.dto.response;

import lombok.Data;

import java.time.Instant;

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
  private Instant createdAt;
}

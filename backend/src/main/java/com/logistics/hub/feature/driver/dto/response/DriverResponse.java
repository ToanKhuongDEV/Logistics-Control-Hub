package com.logistics.hub.feature.driver.dto.response;

import lombok.Data;

import java.time.Instant;

@Data
public class DriverResponse {
    private Long id;
    private String name;
    private String licenseNumber;
    private String phoneNumber;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;
}

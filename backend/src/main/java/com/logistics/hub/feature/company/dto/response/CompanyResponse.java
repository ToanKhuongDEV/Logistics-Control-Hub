package com.logistics.hub.feature.company.dto.response;

import lombok.Data;
import java.time.Instant;

@Data
public class CompanyResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String website;
    private String taxId;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
}

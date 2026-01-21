package com.logistics.hub.feature.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatcherDTO {
    private Long id;
    private String username;
    private String fullName;
    private String role;
    private Boolean active;
}

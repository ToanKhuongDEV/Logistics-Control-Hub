package com.logistics.hub.feature.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DispatcherResponse {
    private Long id;
    private String username;
    private String fullName;
    private String role;
}

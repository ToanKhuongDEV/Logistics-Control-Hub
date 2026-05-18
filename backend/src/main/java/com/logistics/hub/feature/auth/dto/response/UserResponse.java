package com.logistics.hub.feature.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Long driverId;
    private String driverName;
    private List<String> permissions;
    private List<AssignedDepotResponse> assignedDepots;
}

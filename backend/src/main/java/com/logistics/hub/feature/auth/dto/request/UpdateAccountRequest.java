package com.logistics.hub.feature.auth.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.List;

@Data
public class UpdateAccountRequest {

    private String fullName;

    @Email(message = "Email khong hop le")
    private String email;

    private String role;

    private List<Long> assignedDepotIds;
}

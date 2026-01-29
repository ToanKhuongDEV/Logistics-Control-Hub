package com.logistics.hub.feature.auth.dto.request;

import com.logistics.hub.feature.auth.constant.AuthConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = AuthConstant.USERNAME_REQUIRED)
    private String username;

    @NotBlank(message = AuthConstant.PASSWORD_REQUIRED)
    private String password;
}

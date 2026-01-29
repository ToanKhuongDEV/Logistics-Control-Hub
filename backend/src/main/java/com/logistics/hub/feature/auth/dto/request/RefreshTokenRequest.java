package com.logistics.hub.feature.auth.dto.request;

import com.logistics.hub.feature.auth.constant.AuthConstant;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = AuthConstant.REFRESH_TOKEN_REQUIRED)
    private String refreshToken;
}

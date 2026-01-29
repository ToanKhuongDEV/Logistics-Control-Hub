package com.logistics.hub.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.hub.common.base.ApiResponse;
import com.logistics.hub.common.constant.SecurityConstant;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom handler for access denied scenarios
 * Returns 403 Forbidden with consistent ApiResponse format
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException, ServletException {
        
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpStatus.FORBIDDEN.value());

        ApiResponse<Void> apiResponse = ApiResponse.error(
                HttpStatus.FORBIDDEN.value(),
                SecurityConstant.FORBIDDEN
        );

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}

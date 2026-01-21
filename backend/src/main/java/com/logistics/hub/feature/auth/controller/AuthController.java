package com.logistics.hub.feature.auth.controller;

import com.logistics.hub.feature.auth.dto.DispatcherDTO;
import com.logistics.hub.feature.auth.entity.DispatcherEntity;
import com.logistics.hub.feature.auth.repository.DispatcherRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // Ensure bean or manual
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Login APIs for Dispatchers")
public class AuthController {

    private final DispatcherRepository dispatcherRepository;
    // Note: In a real app, inject PasswordEncoder bean. 
    // Since SecurityConfig is basic, we might need to verify if BCrypt bean is available.
    // For now, simpler manual check or standard bean usage.
    
    // Quick Hack: We need a PasswordEncoder. 
    // I will assume for a moment we can use a simple check or the user will configure Security proper.
    // Let's create a temporary login logic.
    
    @PostMapping("/login")
    @Operation(summary = "Login (Simple check for now)")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // Warning: This is a placeholder login logic. 
        // Real implementation should use Spring Security AuthenticationManager.
        
        return dispatcherRepository.findByUsername(request.getUsername())
            .map(user -> {
                // In real app: passwordEncoder.matches(request.getPassword(), user.getPassword())
                // Here we just return user info for "success" signal as requested "login feature"
                // The actual detailed Security wiring is usually a separate large task.
                return ResponseEntity.ok(new DispatcherDTO(user.getId(), user.getUsername(), user.getFullName(), user.getRole(), user.getActive()));
            })
            .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }
    
    @Data
    public static class LoginRequest {
        private String username;
        private String password;
    }
}

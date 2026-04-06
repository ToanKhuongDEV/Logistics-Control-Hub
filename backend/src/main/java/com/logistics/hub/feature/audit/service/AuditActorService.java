package com.logistics.hub.feature.audit.service;

import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditActorService {

    private final UserRepository userRepository;

    public UserEntity getCurrentActor() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        return userRepository.findByUsernameWithAssignedDepots(authentication.getName()).orElse(null);
    }
}

package com.logistics.hub.feature.auth.security;

import com.logistics.hub.feature.auth.entity.DispatcherEntity;
import com.logistics.hub.feature.auth.repository.DispatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * Custom UserDetailsService to load user from database
 * Used by Spring Security and JwtAuthenticationFilter
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final DispatcherRepository dispatcherRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. Find user in database
        DispatcherEntity dispatcher = dispatcherRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 2. Check if user is active
        if (Boolean.FALSE.equals(dispatcher.getActive())) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        // 3. Build and return UserDetails
        return new User(
                dispatcher.getUsername(),
                dispatcher.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + dispatcher.getRole()))
        );
    }
}

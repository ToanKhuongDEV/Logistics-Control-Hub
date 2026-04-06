package com.logistics.hub.feature.auth.security;

import com.logistics.hub.feature.user.entity.UserEntity;
import com.logistics.hub.feature.user.repository.UserRepository;
import com.logistics.hub.feature.auth.policy.AuthorizationPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Set<SimpleGrantedAuthority> authorities = new LinkedHashSet<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
        AuthorizationPolicy.permissionsForRole(user.getRole())
                .forEach(permission -> authorities.add(new SimpleGrantedAuthority(permission)));

        return new User(
                user.getUsername(),
                user.getPassword(),
                authorities);
    }
}

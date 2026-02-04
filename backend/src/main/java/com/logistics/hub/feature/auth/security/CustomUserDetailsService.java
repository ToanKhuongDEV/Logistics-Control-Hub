package com.logistics.hub.feature.auth.security;

import com.logistics.hub.feature.dispatcher.entity.DispatcherEntity;
import com.logistics.hub.feature.dispatcher.repository.DispatcherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final DispatcherRepository dispatcherRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        DispatcherEntity dispatcher = dispatcherRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return new User(
                dispatcher.getUsername(),
                dispatcher.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + dispatcher.getRole())));
    }
}

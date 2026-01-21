package com.logistics.hub.feature.auth.security;

import com.logistics.hub.feature.auth.util.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT Authentication Filter
 * Runs before each request to validate JWT token and set SecurityContext
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        
        // 1. Extract Authorization header
        final String authHeader = request.getHeader("Authorization");
        
        // 2. Check if header exists and starts with "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);
        
        try {
            // 4. Extract username from token
            final String username = jwtUtils.extractUsername(jwt);

            // 5. If username found and not already authenticated
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // 6. Load user details from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // 7. Validate token
                if (jwtUtils.isTokenValid(jwt, userDetails)) {
                    
                    // 8. Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // 9. Set request details
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 10. Set authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token invalid or expired - continue without authentication
            logger.debug("JWT validation failed: " + e.getMessage());
        }

        // 11. Continue filter chain
        filterChain.doFilter(request, response);
    }
}

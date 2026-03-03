package com.api.File.FileProcessing.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.api.File.FileProcessing.services.JwtUtil;

import java.io.IOException;

@Component  // Marks this class as a Spring bean to be auto-detected
public class JwtFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService; // Service to load user info from DB
    private final JwtUtil jwtService;                    // Utility for JWT operations (extract/validate)

    // Constructor injection
    public JwtFilter(UserDetailsService userDetailsService, JwtUtil jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    // This method runs once per request to check JWT authentication
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // ✅ Skip JWT validation for authentication-related endpoints (login, register, token generation)
        if (path.startsWith("/auth")) {
            filterChain.doFilter(request, response);
            return; // Exit early for public endpoints
        }

        // Extract Authorization header
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        // If header exists and starts with "Bearer ", extract JWT token
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);               // Remove "Bearer " prefix
            username = jwtService.extractUsername(token);  // Get username from token
        }

        // If we got a username and no authentication is set yet
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // Load user details from DB
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Validate token against user details
            if (jwtService.validateToken(token, userDetails)) {
                // Create authentication object
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());

                // Attach request-specific details
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Store authentication in the SecurityContext (marks user as authenticated)
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continue filter chain (move request forward)
        filterChain.doFilter(request, response);
    }
}

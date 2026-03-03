package com.api.File.FileProcessing.config;

import com.api.File.FileProcessing.security.JwtFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration                       // Marks this class as a Spring configuration class
@EnableWebSecurity                   // Enables Spring Security for the application
public class SecurityConfig {

    // Custom JWT filter to check tokens in requests
    private final JwtFilter jwtAuthFilter;

    // Service that loads user details from DB
    private final UserDetailsService userDetailsService;

    // Constructor injection (with @Lazy to prevent circular dependencies)
    public SecurityConfig(@Lazy JwtFilter jwtAuthFilter, @Lazy UserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    // Defines the main security filter chain for handling HTTP requests
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF protection (not needed for stateless REST APIs using JWT)
            .csrf(csrf -> csrf.disable())

            // Define which endpoints are public and which require authentication
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/auth/welcome",       // Public endpoint
                    "/auth/addNewUser",    // Public endpoint to register
                    "/auth/generateToken", // Public endpoint to generate JWT
                    "/api/apps/**"         // Public access for app APIs
                ).permitAll()
                .anyRequest().authenticated() // All other requests require authentication
            )

            // Use stateless sessions (JWT manages authentication, no session stored on server)
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Set authentication provider (connects UserDetailsService with PasswordEncoder)
            .authenticationProvider(authenticationProvider())

            // Add the JWT filter before Spring Security's username-password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        // Build and return security configuration
        return http.build();
    }

    // Define password encoder bean (using BCrypt for hashing passwords)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Configure authentication provider with custom UserDetailsService and password encoder
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService); // Set how users are fetched
        provider.setPasswordEncoder(passwordEncoder());     // Set how passwords are checked
        return provider;
    }

    // Provide AuthenticationManager bean (used by Spring Security for authentication process)
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

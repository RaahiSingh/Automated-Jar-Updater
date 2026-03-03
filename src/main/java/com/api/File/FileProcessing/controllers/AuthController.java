package com.api.File.FileProcessing.controllers;

import com.api.File.FileProcessing.entities.User;
import com.api.File.FileProcessing.models.AuthRequest;
import com.api.File.FileProcessing.services.AuthService;
import com.api.File.FileProcessing.services.JwtUtil;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*") // Allow requests from any origin (CORS)
@RestController              // Marks this class as a REST controller
@RequestMapping("/auth")     // Base URL for all endpoints in this controller
public class AuthController {

    // Dependencies injected via constructor
    private AuthService service;                // Handles user-related operations
    private JwtUtil jwtService;                 // Handles JWT token creation & validation
    private AuthenticationManager authenticationManager; // Used for authentication

    // Constructor-based dependency injection
    public AuthController(AuthService service, JwtUtil jwtService, AuthenticationManager authenticationManager) {
        this.service = service;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    // ✅ Public (no authentication required)
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome this endpoint is not secure";
    }

    // ✅ Register new user
    @PostMapping("/addNewUser")
    public String addNewUser(@RequestBody User userInfo) {
        return service.addUser(userInfo); // Delegates to AuthService (password is encoded there)
    }

    // ✅ Login endpoint: authenticate user & generate JWT token
    @PostMapping("/generateToken")
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate username & password using AuthenticationManager
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(), // Input username
                    authRequest.getPassword()  // Input password
                )
            );

            // If authentication is successful → generate JWT
            if (authentication.isAuthenticated()) {
                return ResponseEntity.ok(jwtService.generateToken(authRequest.getUsername()));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user request!");
            }
        } catch (Exception e) {
            e.printStackTrace(); // Debugging purpose (better to use a logger in production)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                 .body("Authentication failed: " + e.getMessage());
        }
    }
}

package com.api.File.FileProcessing.models;

// This class represents the response that will be sent after a successful authentication
// It typically contains the JWT token generated upon login
public class AuthResponse {
    private String token; // The JWT token that will be returned to the client

    // Constructor to initialize the token when creating a response
    public AuthResponse(String token) {
        this.token = token;
    }

    // Getter method to retrieve the token
    public String getToken() {
        return token;
    }

    // Setter method to update the token value if needed
    public void setToken(String token) {
        this.token = token;
    }
}

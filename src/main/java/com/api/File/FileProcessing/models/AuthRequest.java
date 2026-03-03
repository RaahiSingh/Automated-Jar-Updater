package com.api.File.FileProcessing.models;

// This class represents an authentication request model (used for login).
// It usually carries the username and password from client to server.
public class AuthRequest {

    // Field to store the username
    private String username;

    // Field to store the password
    private String password;

    // Default no-argument constructor (needed by frameworks like Spring for deserialization)
    public AuthRequest() {}

    // Parameterized constructor to easily create AuthRequest objects with username & password
    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter method for username
    public String getUsername() {
        return username;
    }

    // Getter method for password
    public String getPassword() {
        return password;
    }

    // Setter method for username
    public void setUsername(String username) {
        this.username = username;
    }

    // Setter method for password
    public void setPassword(String password) {
        this.password = password;
    }
}

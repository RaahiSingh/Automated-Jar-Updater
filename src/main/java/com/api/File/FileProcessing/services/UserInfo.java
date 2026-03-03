package com.api.File.FileProcessing.services;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import com.api.File.FileProcessing.entities.User;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

// This class is a custom implementation of Spring Security's UserDetails
// It adapts your User entity to be used by Spring Security for authentication and authorization
public class UserInfo implements UserDetails {

    private String username;                  // Stores username of the user
    private String password;                  // Stores password of the user
    private List<GrantedAuthority> authorities; // Stores roles/authorities assigned to the user

    // Constructor: takes your custom User entity and maps it to Spring Security's UserDetails
    public UserInfo(User user) {
        this.username = user.getUsername();   // Fetch username from User entity
        this.password = user.getPassword();   // Fetch password from User entity
        this.authorities = new ArrayList<>(); // Initialize empty authorities (roles can be added here if available)
    }

    // Returns the authorities (roles/permissions) of the user
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // Returns username (required by Spring Security)
    @Override
    public String getUsername() {
        return username;
    }

    // Returns password (required by Spring Security)
    @Override
    public String getPassword() {
        return password;
    }

    // Allows updating the password if needed
    public void setPassword(String password) {
        this.password = password;
    }

    // The following methods represent the account status
    // For now, all return true (account is active and not restricted)

    // Account is not expired
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // Account is not locked
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // Credentials (password) are not expired
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Account is enabled
    @Override
    public boolean isEnabled() {
        return true;
    }
}

// Package name
package com.api.File.FileProcessing.services;

// Importing Spring Security classes for user authentication
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// Importing our custom User entity and repository
import com.api.File.FileProcessing.entities.User;
import com.api.File.FileProcessing.repositories.UserRepository;

// Importing utilities
import java.util.ArrayList;
import java.util.Optional;

// Mark this class as a Spring Service (business logic class)
@Service
// AuthService implements Spring Security's UserDetailsService
public class AuthService implements UserDetailsService {

    // Repository to fetch User data from DB
    private UserRepository repository = null;
    // Password encoder to encrypt passwords
    private PasswordEncoder encoder = null;

    // Constructor injection for repository and encoder
    public AuthService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    // Method required by Spring Security: load user by username
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Print which username is being checked
        System.out.println("Checking user: " + username);

        // Search user in DB using repository
        Optional<User> userInfo = repository.findByUsername(username);

        // Print whether user is found
        System.out.println("Found: " + userInfo.isPresent());

        // If user not found, throw exception
        if (userInfo.isEmpty()) {
            throw new UsernameNotFoundException("User not found " + username);
        }
        
        // Get user object
        User user = userInfo.get();

        // Convert User entity into Spring Security's UserDetails object
        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),   // username
            user.getPassword(),   // encoded password
            new ArrayList<>()     // empty roles/authorities list for now
        );
    }

    // Method to register (add) a new user
    public String addUser(com.api.File.FileProcessing.entities.User userInfo) {
        // Encrypt the password before saving to DB
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        
        // Save the user to DB
        repository.save(userInfo);  

        // Return success message
        return "User added successfully!";
    }

}

package com.api.File.FileProcessing.services;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import io.jsonwebtoken.security.Keys;

@Component // Marks this class as a Spring Bean
public class JwtUtil {

    // Secret key (not used directly here since a new key is generated dynamically below)
    private static final String SECRET = "myverysecretkey1234567890abcd";

    // Generate JWT token for a given username (usually email)
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>(); // Extra payload if needed
        return createToken(claims, username);
    }

    // ✅ Key generated dynamically for HS256 signing
    // Note: This means tokens will be invalid after application restart
    Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Build and sign the token
    private String createToken(Map<String, Object> claims, String username) {
        return Jwts.builder()
                .setClaims(claims)                                  // Custom claims (empty in this case)
                .setSubject(username)                               // Store username (subject)
                .setIssuedAt(new Date())                            // Issue time
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // Expiry = 10 hours
                .signWith(key)                                      // Sign with secret key
                .compact();
    }

    // Extract username (subject) from token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract expiration date from token
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic claim extractor using functional interface
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parse and return all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key) // Use same key used for signing
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Validate token against user details
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token); // Get username from token
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

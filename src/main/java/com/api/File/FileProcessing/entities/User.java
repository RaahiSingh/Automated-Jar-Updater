// Package where this entity belongs
package com.api.File.FileProcessing.entities;

// Importing JPA annotations for entity mapping
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Mark this class as a JPA entity (maps to DB table)
@Entity
// Specify table name as "users"
@Table(name = "users")
public class User {

    // Primary key column (id)
    @Id
    // Auto-generate id values (auto increment in DB)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    // "username" column - cannot be null and must be unique
    @Column(nullable = false, unique = true)
    private String username;

    // "password" column
    private String password;

    // Getter for id
    public long getId() {
        return id;
    }

    // Setter for id
    public void setId(long id) {
        this.id = id;
    }

    // Getter for username
    public String getUsername() {
        return username;
    }

    // Setter for username
    public void setUsername(String usermame) {
        this.username = usermame;
    }

    // Getter for password
    public String getPassword() {
        return password;
    }

    // Setter for password
    public void setPassword(String password) {
        this.password = password;
    }

}

package com.api.File.FileProcessing.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.api.File.FileProcessing.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);  // This must match the field name in the User entity
}

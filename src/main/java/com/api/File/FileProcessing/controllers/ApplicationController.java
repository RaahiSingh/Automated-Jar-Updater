// Package where this controller belongs
package com.api.File.FileProcessing.controllers;

// Importing annotations and classes for REST APIs
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Importing classes for reading and handling files
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

// Importing Spring dependencies
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// Importing custom entities, repositories, and services
import com.api.File.FileProcessing.entities.ApplicationInfo;
import com.api.File.FileProcessing.repositories.ApplicationRepository;
import com.api.File.FileProcessing.services.AutoUpdate;
import com.api.File.FileProcessing.services.DBConnection;

// Importing exception class
import io.jsonwebtoken.io.IOException;

// Marking this as a REST Controller
@RestController
// Base URL for all APIs in this controller
@RequestMapping("/api/apps")
// Allowing requests from different origins
@CrossOrigin
public class ApplicationController {

    // Automatically injecting ApplicationRepository
    @Autowired
    private ApplicationRepository appRepo;

    // Automatically injecting AutoUpdate service
    @Autowired
    private AutoUpdate autoUpdate;

    // API to check status of running applications
    @GetMapping("/check-status")
    public String checkAppStatuses() {
        // Calls service method to check and update status of running JARs
        autoUpdate.checkRunningJarsAndUpdateStatus();
        // Returns response message
        return "Application statuses checked and updated.";
    }

    // API to get all applications from database
    @GetMapping
    public List<ApplicationInfo> getAllapps() {
        // Finds and returns all applications
        return appRepo.findAll();
    }

    // API to restart application by id
    @PostMapping("/restart/{id}")
    public String restartApp(@PathVariable Long id) throws java.io.IOException, SQLException {
        // Find application by id (wrapping in Optional)
        Optional<ApplicationInfo> optionalApp = Optional.ofNullable(appRepo.findById(id));
        // If app not found return message
        if (!optionalApp.isPresent()) {
            return "Application not found";
        }
    
        // Get the application from optional
        ApplicationInfo app = optionalApp.get();
    
        // Full path of jar file (taken from DB)
        String jarFullPath = app.getJarPath(); 
    
        // Path of java executable
        String javaPath = "java";
    
        try {
            // Print message in console for debugging
            System.out.println("Attempting to restart: " + jarFullPath);
        
            // Create process builder to run the jar
            ProcessBuilder pb = new ProcessBuilder(javaPath, "-jar", jarFullPath);
            // Redirect error messages to same stream
            pb.redirectErrorStream(true);
        
            // Start the process
            Process process = pb.start();
            // Capture PID of the newly started process
            long pid = process.pid();
            String updateQuery = "UPDATE application_info.info SET appStatus=?, pid=? WHERE id=?";
            try (Connection cn = DBConnection.getConnection();
                 PreparedStatement stmt = cn.prepareStatement(updateQuery)) {

                stmt.setString(1, "Running");
                stmt.setLong(2, pid);
                stmt.setLong(3, app.getId());
                stmt.executeUpdate();
            }
        
            // Capture logs from process in a new thread
            new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    // Keep reading logs line by line
                    while ((line = reader.readLine()) != null) {
                        System.out.println("[RESTART LOG] " + line);
                    }
                } catch (IOException e) {
                    // Handle exception
                    e.printStackTrace();
                } catch (java.io.IOException e) {
                    // Handle Java IO exception
                    e.printStackTrace();
                }
            }).start();
        
            // Return success message
            return "Restart command sent for: " + app.getAppName();
        
        } catch (IOException e) {
            // Print error and return failure message
            e.printStackTrace();
            return "Error restarting app: " + e.getMessage();
        }
    }

    @PostMapping("/stop/{id}")
    public String stopJar(@PathVariable Long id) {
        // Get DB connection
        try (Connection cn = DBConnection.getConnection()) {
            String selectQuery = "SELECT pid, jarpath FROM application_info.info WHERE id = ?";
            try (PreparedStatement stmt = cn.prepareStatement(selectQuery)) {
                stmt.setLong(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    long pid = rs.getLong("pid");
                    String jarPath = rs.getString("jarpath");

                    if (pid > 0) {
                        // Kill the process
                        ProcessHandle.of(pid).ifPresent(ProcessHandle::destroy);
                        System.out.println(" Stopped JAR: " + jarPath + " (PID=" + pid + ")");

                        // Update DB status
                        String updateQuery = "UPDATE application_info.info SET appStatus = 'Stopped' WHERE id = ?";
                        try (PreparedStatement updateStmt = cn.prepareStatement(updateQuery)) {
                            updateStmt.setLong(1, id);
                            updateStmt.executeUpdate();
                        }

                        return "JAR stopped successfully";
                    } else {
                        return "JAR is not running";
                    }
                } else {
                    return "JAR not found";
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "Error stopping JAR: " + e.getMessage();
        }
    }

    // API to upgrade an app by uploading a new JAR
    @PutMapping("/upgrade/{id}")
    public ResponseEntity<String> upgradeApp(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file) {

        // Check if file is empty or not provided
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("No file uploaded or file is empty.");
        }

        // Get name of uploaded file
        String newJarPath = file.getOriginalFilename();

        try {
            // Save uploaded file to disk (overwrite if exists)
            Files.copy(file.getInputStream(), Paths.get(newJarPath), StandardCopyOption.REPLACE_EXISTING);

            // Update jar path in database using service
            autoUpdate.updateJarPath(id, newJarPath);

            // Return success response
            return ResponseEntity.ok("Application upgraded successfully!");
        } catch (IOException e) {
            // Handle IO errors
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upgrade failed due to IO error: " + e.getMessage());
        } catch (Exception e) {
            // Handle other errors
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Upgrade failed: " + e.getMessage());
        }
    }

}

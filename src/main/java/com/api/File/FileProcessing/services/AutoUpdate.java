// Package name
package com.api.File.FileProcessing.services;

import org.springframework.scheduling.annotation.Scheduled;
// Importing Spring annotation for service
import org.springframework.stereotype.Service;

// Importing classes for reading process output and database handling
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;

// Marking this class as a Spring Service (used for business logic)
@Service
public class AutoUpdate {

    // Method to check running JARs and update status in DB
    public void checkRunningJarsAndUpdateStatus() {
        // Get currently running JAR processes
        List<String> runningJars = getRunningJars();
        System.out.println("✔️ Running JARs detected: " + runningJars);

        // Try-with-resources to auto close connection
        try (Connection cn = DBConnection.getConnection()) {

            // Store jarpath and id from DB in a map
            Map<String, Long> dbJarMap = new HashMap<>();
            String selectQuery = "SELECT id, jarpath FROM application_info.info";

            // Execute select query to get existing records
            try (PreparedStatement selectStmt = cn.prepareStatement(selectQuery);
                 ResultSet rs = selectStmt.executeQuery()) {

                // Put jarpath and id into the map
                while (rs.next()) {
                    dbJarMap.put(rs.getString("jarpath"), rs.getLong("id"));
                }
            }

            // Query to insert new JARs that are running but not in DB
            String insertQuery = "INSERT INTO application_info.info (appName, jarpath, appStatus) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = cn.prepareStatement(insertQuery)) {
                for (String runningJar : runningJars) {
                    // Split process output line into parts
                    String[] parts = runningJar.trim().split("\\s+");
                    if (parts.length >= 2) {
                        String jarPath = parts[1];

                        // If this jarPath is not already in DB
                        if (!dbJarMap.containsKey(jarPath)) {
                            // Extract only the jar filename from path
                            String jarFile = jarPath.substring(jarPath.lastIndexOf("/") + 1);  // or "\\" for Windows
                            // Remove version numbers from filename to get appName
                            String appName = jarFile.replaceAll("(-|_)[0-9].*", "").replace(".jar", "");

                            // Insert into DB
                            insertStmt.setString(1, appName);
                            insertStmt.setString(2, jarPath);
                            insertStmt.setString(3, "Running");
                            insertStmt.executeUpdate();

                            System.out.println("🆕 Inserted JAR: " + appName + " → " + jarPath);
                        }
                    }
                }
            }

            // Query to update appStatus of existing jars
            // Query to update appStatus of existing jars
        String updateQuery = "UPDATE application_info.info SET appStatus = ?, pid = ? WHERE id = ?";
                
        try (PreparedStatement updateStmt = cn.prepareStatement(updateQuery)) {
            for (Map.Entry<String, Long> entry : dbJarMap.entrySet()) {
                String jarPath = entry.getKey();
                Long id = entry.getValue();
            
                // Find match from runningJars
                Optional<String> match = runningJars.stream()
                        .filter(line -> line.contains(jarPath))
                        .findFirst();
            
                long pid = 0;
                if (match.isPresent()) {
                    String[] parts = match.get().split("\\|");
                    pid = Long.parseLong(parts[0]);  // extract PID
                }
            
                boolean isRunning = pid > 0 && ProcessHandle.of(pid).map(ProcessHandle::isAlive).orElse(false);
                String newStatus = isRunning ? "Running" : "Stopped";
            
                // Update DB with correct order of params
                updateStmt.setString(1, newStatus);
                updateStmt.setLong(2, pid);
                updateStmt.setLong(3, id);
                updateStmt.executeUpdate();
            
                System.out.println("🔄 " + jarPath + " → Status updated to: " + newStatus + " (PID=" + pid + ")");
            }
        }
        

        } catch (SQLException e) {
            // Handle database error
            System.err.println("❌ SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to get list of currently running JAR processes
    private List<String> getRunningJars() {
        List<String> jars = new ArrayList<>();
        try {
            // Run "jps -l" command (shows running Java processes)
            ProcessBuilder processBuilder = new ProcessBuilder("jps", "-l");
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Read command output
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;

            // Loop through each line of output
            while ((line = reader.readLine()) != null) {
                // Split line into PID and process name
                String[] parts=line.trim().split("\\s+");
                if(parts.length==2){
                    String pid=parts[0];
                    String jarPath=parts[1];
                
                // Filter only valid .jar processes, ignore IDEs and tools
                if (jarPath.contains(".jar") &&
                        !jarPath.toLowerCase().contains("vscode") &&
                        !jarPath.toLowerCase().contains("equinox") &&
                        !jarPath.toLowerCase().contains("jdk") &&
                        !jarPath.toLowerCase().contains("idea") &&
                        !jarPath.toLowerCase().contains("tools")) {

                    System.out.println("✅ Running JAR: PID=" + pid + " → " + jarPath);
                    jars.add(pid+"|"+jarPath);
                }
            }
            }

            // Close reader
            reader.close();
        } catch (Exception e) {
            // Handle error if process fails
            System.err.println("❌ Error fetching running JARs: " + e.getMessage());
            e.printStackTrace();
        }

        return jars;
    }

    // Method to update jarPath of an app by id
    public void updateJarPath(Long id, String newJarPath) {
        String updateQuery = "UPDATE application_info.info SET jarPath = ? WHERE id = ?";
        // Get DB connection
        Connection cn = DBConnection.getConnection();
        try (PreparedStatement updateStmt = cn.prepareStatement(updateQuery)) {
            // Set new values
            updateStmt.setString(1, newJarPath);
            updateStmt.setLong(2, id);

            // Execute update
            int rowsUpdated = updateStmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("✅ Jar path updated for id: " + id);
            } else {
                System.out.println("⚠️ No record found with id: " + id);
            }
        } catch (SQLException e) {
            // Handle SQL error
            System.err.println("❌ SQL Error while updating jar path: " + e.getMessage());
            e.printStackTrace();
        }
    }
    @Scheduled(fixedRate = 5000)
    public void refreshStatuses() {
        checkRunningJarsAndUpdateStatus();
    }


}

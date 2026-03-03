package com.api.File.FileProcessing.entities;

// Represents information about an application in the system
public class ApplicationInfo {
    private long id;          // Unique identifier for the application
    private String appName;   // Name of the application
    private String appStatus; // Current status of the application (e.g., Running, Stopped)
    private String jarPath;   // Path to the application's JAR file
    private long pid;         // Process ID of the running JAR

    // Getters and Setters

    public long getId() {
        return id; // Returns the application ID
    }

    public void setId(long id) {
        this.id = id; // Sets the application ID
    }

    public String getAppName() {
        return appName; // Returns the application name
    }

    public void setAppName(String appName) {
        this.appName = appName; // Sets the application name
    }

    public String getAppStatus() {
        return appStatus; // Returns the application status
    }

    public void setAppStatus(String appStatus) {
        this.appStatus = appStatus; // Sets the application status
    }

    public String getJarPath() {
        return jarPath; // Returns the JAR file path
    }

    public void setJarPath(String jarPath) {
        this.jarPath = jarPath; // Sets the JAR file path
    }

    public long getPid() {
        return pid; // Returns the process ID
    }

    public void setPid(long pid) {
        this.pid = pid; // Sets the process ID
    }
}

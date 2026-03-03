# Automated Jar Updater

This Spring Boot project automates the management of JAR-based services.  

It allows users to:  
- View running services and their statuses  
- Stop or restart services  
- Upload new JAR versions to update applications  

A simple web dashboard built with HTML, Bootstrap, and JavaScript provides an easy interface for these operations. Authentication is handled using JWT tokens for security.  

## Features

- Displays service status in a dashboard  
- Stop or restart any service with a click  
- Upgrade applications by uploading new JAR files  
- JWT-based authentication for secure access  
- File upload limits and validation included  

## Tech Stack

- Backend: Java, Spring Boot, Spring Security, Spring Data JPA  
- Frontend: HTML, Bootstrap, JavaScript  
- Database: MySQL  
- Security: JWT authentication  

## Setup

1. Update `application.properties` with your MySQL credentials.  
2. Build and run the project using Maven:  
   
   `.\mvnw.cmd clean install -DskipTests`
   
   `java -jar target/<your-built-jar>.jar`
4. Open `Login.html` in your browser, log in using your credentials, and then access the dashboard to manage JAR services.

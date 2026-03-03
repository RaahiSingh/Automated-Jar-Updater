package com.api.File.FileProcessing.services;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {
    static Connection connection;
    public static Connection getConnection() {
        try {
            System.out.println("Inside getConnection()");

            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/application_info", "your_username", "your_password");
            System.out.println("Connected to DB");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

}
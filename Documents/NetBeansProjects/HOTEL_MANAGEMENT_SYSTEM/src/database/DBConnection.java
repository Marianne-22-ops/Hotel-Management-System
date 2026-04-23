package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    public static Connection connect() {

        try {

            Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3307/hotel_management_system?zeroDateTimeBehavior=CONVERT_TO_NULL",
                    "root",
                    "hotelsystem123"
            );

            return conn;

        } catch (SQLException e) {

            System.out.println("Database connection failed: " + e.getMessage());
            return null;

        }

    }

    public static Connection getConnection() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
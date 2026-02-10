package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class dbconnect {

    private static final String URL =
            "jdbc:mysql://localhost:3306/mindcare?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = ""; // XAMPP default

    // Singleton instance
    private static dbconnect instance;

    // Single Connection object
    private Connection connection;

    // Private constructor
    private dbconnect() {
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to database", e);
        }
    }

    // Public access point
    public static synchronized dbconnect getInstance() {
        if (instance == null) {
            instance = new dbconnect();
        }
        return instance;
    }

    // Get the single connection
    public Connection getConnection() {
        return connection;
    }
}

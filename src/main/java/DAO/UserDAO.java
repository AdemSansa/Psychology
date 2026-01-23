package DAO;

import Database.dbconnect;
import Entities.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    public boolean usernameExists(String username) throws Exception {
        String sql = "SELECT id FROM users WHERE username = ?";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public void save(User user) throws Exception {
        String sql = """
            INSERT INTO users (full_name, username, email, password, role)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getUsername());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getPassword());
            stmt.setString(5, user.getRole());

            stmt.executeUpdate();
        }
    }
    public void delete(User user) throws Exception {
        String sql = "DELETE FROM users WHERE username = ?";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.executeUpdate();
        }
    }
    public void update(User user) throws Exception {
        String sql = """
            UPDATE users
            SET full_name = ?, email = ?, password = ?, role = ?
            WHERE username = ?
        """;

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getFullName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, user.getPassword());
            stmt.setString(4, user.getRole());
            stmt.setString(5, user.getUsername());

            stmt.executeUpdate();
        }
    }
    public boolean emailExists(String email) throws Exception {
        String sql = "SELECT id FROM users WHERE email = ?";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            rs.next();
        }
        return false;
    }
}

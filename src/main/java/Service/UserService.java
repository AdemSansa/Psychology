package Service;

import Database.dbconnect;
import Entities.User;
import interfaces.Iservice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserService implements Iservice<User> {

    @Override
    public void create(User user) throws SQLException {
        String requete = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?,?,?,?,?)";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        statement.setString(3, user.getEmail());
        statement.setString(4, user.getPassword());
        statement.setString(5, user.getRole());
        statement.executeUpdate();
        System.out.println("User added successfully!");
    }

    @Override
    public List<User> list() throws SQLException {
        String requete = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            users.add(mapResultSetToUser(rs));
        }
        return users;
    }

    @Override
    public User read(int id) throws SQLException {
        String requete = "SELECT * FROM users WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return mapResultSetToUser(rs);
        }
        return null;
    }

    @Override
    public void update(User user) throws SQLException {
        String requete = "UPDATE users SET first_name = ?, last_name = ?, email = ?, password = ?, role = ? WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        statement.setString(3, user.getEmail());
        statement.setString(4, user.getPassword());
        statement.setString(5, user.getRole());
        statement.setInt(6, user.getId());
        statement.executeUpdate();
        System.out.println("User updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {
        String requete = "DELETE FROM users WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setInt(1, id);
        statement.executeUpdate();
        System.out.println("User deleted successfully!");
    }

    // ------------------- Méthode de recherche multi-critères -------------------
    public List<User> rechercher(String motCle) throws SQLException {
        String requete = "SELECT * FROM users WHERE "
                + "id LIKE ? OR "
                + "first_name LIKE ? OR "
                + "last_name LIKE ? OR "
                + "email LIKE ? OR "
                + "role LIKE ?";

        List<User> users = new ArrayList<>();
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        String mot = "%" + motCle + "%";
        statement.setString(1, mot);
        statement.setString(2, mot);
        statement.setString(3, mot);
        statement.setString(4, mot);
        statement.setString(5, mot);

        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            users.add(mapResultSetToUser(rs));
        }
        return users;
    }

    // ------------------- Vérifie si un email existe -------------------
    public boolean emailExists(String email) throws SQLException {
        String requete = "SELECT COUNT(*) FROM users WHERE email = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }

    // ------------------- Mise à jour mot de passe -------------------
    public void updatePassword(String email, String hashedPassword) throws SQLException {
        String requete = "UPDATE users SET password = ? WHERE email = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setString(1, hashedPassword);
        statement.setString(2, email);
        statement.executeUpdate();
        System.out.println("User password updated successfully!");
    }

    // ------------------- Lecture par email -------------------
    public User readByEmail(String email) throws SQLException {
        String requete = "SELECT * FROM users WHERE email = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return mapResultSetToUser(rs);
        }
        return null;
    }

    // ------------------- Mapping ResultSet vers User -------------------
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setEmail(rs.getString("email"));
        u.setRole(rs.getString("role"));
        u.setPassword(rs.getString("password"));
        return u;
    }
}

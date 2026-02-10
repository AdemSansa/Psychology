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

        String Requete = "INSERT INTO users (full_name, username, email, password) VALUES (?,?,?,? )";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
        statement.setString(1, user.getFullName());
        statement.setString(2, user.getUsername());
        statement.setString(3, user.getEmail());
        statement.setString(4, user.getPassword());
        statement.executeUpdate();
        System.out.println("User added successfully!");
    }

    @Override
    public List<User> list() throws SQLException {

        String Requete = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                User u = new User();
                u.setFullName(rs.getString("full_name"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setId(rs.getInt("id"));
                u.setRole(rs.getString("role"));

                u.setPassword(rs.getString("password"));
                users.add(u);
            }
        }
        return users;
    }

    @Override
    public User read(int id) throws SQLException {
        String requete = "SELECT * FROM users WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();
        User user = null;
        if (rs.next()) {
            user = new User();
            user.setId(rs.getInt("id"));
            user.setFullName(rs.getString("full_name"));
            user.setUsername(rs.getString("username"));
            user.setEmail(rs.getString("email"));
            user.setRole(rs.getString("role"));
            user.setPassword(rs.getString("password"));
        }
        System.out.println(user);
        return user;
    }

    @Override
    public void update(User user) throws SQLException {
        String Requete = "UPDATE users SET full_name = ?, username = ?, email = ?, password = ? WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
        statement.setString(1, user.getFullName());
        statement.setString(2, user.getUsername());
        statement.setString(3, user.getEmail());
        statement.setString(4, user.getPassword());
        statement.setInt(5, user.getId());
        statement.executeUpdate();
        System.out.println("User updated successfully!");

    }

    @Override
    public void delete(int id) throws SQLException {
        String Requete = "DELETE FROM users WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
        statement.setInt(1, id);
        statement.executeUpdate();
        System.out.println("User deleted successfully!");


    }
    public boolean UserNameExists(String username) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(query);
        statement.setString(1, username);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
    public boolean emailExists(String email) throws SQLException {
        String query = "SELECT COUNT(*) FROM users WHERE email = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(query);
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}

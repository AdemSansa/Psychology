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
        String Requete = "INSERT INTO users (first_name, last_name, email, password, role) VALUES (?,?,?,?,?)";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
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
        String Requete = "SELECT * FROM users";
        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
                ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                User u = mapResultSetToUser(rs);
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
            user = mapResultSetToUser(rs);
        }
        return user;
    }

    @Override
    public void update(User user) throws SQLException {
        String Requete = "UPDATE users SET first_name = ?, last_name = ?, email = ?, password = ?, role = ? WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
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
        String Requete = "DELETE FROM users WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(Requete);
        statement.setInt(1, id);
        statement.executeUpdate();
        System.out.println("User deleted successfully!");
    }

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
    public User findUser  (String email) throws SQLException {
        String query = "SELECT * FROM users WHERE email = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(query);
        statement.setString(1, email);
        ResultSet rs = statement.executeQuery();
        if (rs.next()) {
            return mapResultSetToUser(rs);
        }
        return null;
    }
}

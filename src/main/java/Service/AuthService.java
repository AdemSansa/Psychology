package Service;

import Database.dbconnect;
import Entities.Therapistis;
import Entities.User;
import util.PasswordUtil;
import util.SessionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    private final UserService userDAO = new UserService();
    private final TherapistService therapistDAO = new TherapistService();

    public void register(User user) throws Exception {
        validateBasicInfo(user.getEmail(), user.getPassword());

        if (userDAO.emailExists(user.getEmail())) {
            throw new Exception("Email already exists.");
        }

        String hashedPassword = PasswordUtil.hashPassword(user.getPassword());
        user.setPassword(hashedPassword);
        userDAO.create(user);
    }

    public void registerTherapist(Therapistis therapist) throws Exception {
        validateBasicInfo(therapist.getEmail(), therapist.getPassword());

        if (userDAO.emailExists(therapist.getEmail())) {
            throw new Exception("Email already exists in users.");
        }

        String hashedPassword = PasswordUtil.hashPassword(therapist.getPassword());
        therapist.setPassword(hashedPassword);
        therapistDAO.create(therapist);
    }

    private void validateBasicInfo(String email, String password) throws Exception {
        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            throw new Exception("Email and password are required.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Invalid email format.");
        }
        if (password.length() < 6) {
            throw new Exception("Password must be at least 6 characters long.");
        }
    }

    public String login(String email, String password) throws Exception {
        Connection conn = dbconnect.getInstance().getConnection();

        // 1. Check users table (Email only)
        String userQuery = "SELECT * FROM users WHERE email = ?";
        PreparedStatement userStmt = conn.prepareStatement(userQuery);
        userStmt.setString(1, email);
        ResultSet userRs = userStmt.executeQuery();

        if (userRs.next()) {
            String storedHash = userRs.getString("password");
            if (PasswordUtil.verifyPassword(password, storedHash)) {
                User user = new User();
                user.setId(userRs.getInt("id"));
                user.setFirstName(userRs.getString("first_name"));
                user.setLastName(userRs.getString("last_name"));
                user.setEmail(userRs.getString("email"));
                user.setRole(userRs.getString("role"));
                SessionManager.setUserSession(user);
                return user.getRole();
            }
        }

        // 2. Check therapists table (Email only)
        String therapistQuery = "SELECT * FROM therapists WHERE email = ?";
        PreparedStatement therapistStmt = conn.prepareStatement(therapistQuery);
        therapistStmt.setString(1, email);
        ResultSet therapistRs = therapistStmt.executeQuery();

        if (therapistRs.next()) {
            String storedHash = therapistRs.getString("password");
            if (PasswordUtil.verifyPassword(password, storedHash)) {
                Therapistis t = new Therapistis();
                t.setId(therapistRs.getInt("id"));
                t.setFirstName(therapistRs.getString("first_name"));
                t.setLastName(therapistRs.getString("last_name"));
                t.setEmail(therapistRs.getString("email"));
                t.setPassword(storedHash);
                SessionManager.setTherapistSession(t);
                return "therapist";
            }
        }

        throw new Exception("Invalid email or password.");
    }
}
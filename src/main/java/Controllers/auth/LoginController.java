package Controllers.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import Database.dbconnect;
import util.PasswordUtil;
import util.SceneManager;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {

        // (Remove this later – for testing only)
        usernameField.setText("adem.sansa7@gmail.com");
        passwordField.setText("adem2003");

        String email = usernameField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        String query = "SELECT * FROM users WHERE email = ?";

        try {
            // ✅ Get Singleton connection
            Connection conn = dbconnect.getInstance().getConnection();

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");

                if (PasswordUtil.verifyPassword(password, storedHashedPassword)) {
                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Login successful ✔");

                    SceneManager.switchScene("/com/example/psy/intro/Home.fxml");
                } else {
                    showError();
                }
            } else {
                showError();
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database connection error.");
        }
    }

    private void showError() {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText("Invalid email or password.");
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchScene("/com/example/psy/auth/register.fxml");
    }
}

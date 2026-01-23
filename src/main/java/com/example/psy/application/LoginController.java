package com.example.psy.application;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import Database.dbconnect;
import javafx.stage.Stage;
import util.PasswordUtil;
import util.SceneManager;

public class  LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {

        String email = usernameField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        String query = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = dbconnect.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {

                String storedHashedPassword = rs.getString("password");

                // 🔐 VERIFY HASH
                if (PasswordUtil.verifyPassword(password, storedHashedPassword)) {

                    messageLabel.setStyle("-fx-text-fill: green;");
                    messageLabel.setText("Login successful ✔");

                    // TODO: load user + open dashboard

                } else {
                    messageLabel.setStyle("-fx-text-fill: red;");
                    messageLabel.setText("Invalid email or password.");
                }

            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Invalid email or password.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Database connection error.");
        }
    }
    @FXML
    private void goToRegister() {

        SceneManager.switchScene("/com/example/psy/register.fxml");
    }

}
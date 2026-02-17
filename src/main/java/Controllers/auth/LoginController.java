package Controllers.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import Service.AuthService;
import util.PasswordUtil;
import util.SceneManager;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    private void handleLogin() {
        String identifier = usernameField.getText();
        String password = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        try {
            String role = authService.login(identifier, password);
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Login successful ✔");

            // Role-based redirection
            switch (role.toLowerCase()) {
                case "admin":
                    SceneManager.switchScene("/com/example/psy/intro/Home.fxml");
                    break;
                case "therapist":
                    SceneManager.switchScene("/com/example/psy/intro/Home.fxml");
                    break;
                default: // patient or employee
                    SceneManager.switchScene("/com/example/psy/intro/Home.fxml");
                    break;
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message != null ? message : "Invalid login credentials.");
    }

    @FXML
    private void handleForgotPassword() {
        SceneManager.switchScene("/com/example/psy/auth/forgot_password.fxml");
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchScene("/com/example/psy/auth/register.fxml");
    }
}

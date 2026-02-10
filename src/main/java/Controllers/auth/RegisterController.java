package Controllers.auth;

import Entities.User;
import Service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import util.SceneManager;

public class RegisterController {
    @FXML
    private TextField fullNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    private final AuthService authService = new AuthService();

    @FXML
    private void handleRegister() {

        try {
            User user = new User(
                    fullNameField.getText(),
                    usernameField.getText(),
                    emailField.getText(),
                    passwordField.getText()
            );


            authService.register(user);

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account created successfully ✔");

            SceneManager.switchScene("/com/example/psy/auth/login.fxml");

        } catch (Exception e) {
            //empty all fealds
            fullNameField.clear();
            usernameField.clear();
            emailField.clear();
            passwordField.clear();

            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(e.getMessage());
        }
    }
    @FXML
    private void goToLogin() {
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }

}

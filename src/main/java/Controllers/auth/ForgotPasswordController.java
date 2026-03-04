package Controllers.auth;

import Service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import util.SceneManager;

public class ForgotPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private VBox emailStepBox;

    @FXML
    private VBox resetStepBox;

    @FXML
    private TextField codeField;

    private final AuthService authService = AuthService.getInstance();
    private final Service.NotificationService notificationService = Service.NotificationService.getInstance();

    @FXML
    private void handleSendCode() {
        String email = emailField.getText();

        if (email.isEmpty()) {
            showError("Veuillez entrer votre adresse email.");
            return;
        }

        try {
            if (!authService.verifyEmailExists(email)) {
                showError("Email introuvable.");
                return;
            }

            Entities.User user = (new Service.UserService()).readByEmail(email);

            // Send via NotificationService (Email only)
            notificationService.sendPasswordReset(user); // Updated method call

            showSuccess("Code envoyé (vérifiez votre email) !"); // Updated success message

            // Masquer l'étape 1, afficher l'étape 2
            emailStepBox.setVisible(false);
            emailStepBox.setManaged(false);

            resetStepBox.setVisible(true);
            resetStepBox.setManaged(true);

        } catch (Exception e) {
            showError("Erreur d'envoi du code : " + e.getMessage());
        }
    }

    @FXML
    private void handleResetPassword() {
        String email = emailField.getText();
        String code = codeField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (code == null || code.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas.");
            return;
        }

        if (newPassword.length() < 6) {
            showError("Le mot de passe doit contenir au moins 6 caractères.");
            return;
        }

        try {
            // Vérifier le code OTP
            if (!notificationService.verifyCode(email, code)) {
                showError("Code invalide ou expiré.");
                return;
            }

            authService.resetPassword(email, newPassword);
            showSuccess("Mot de passe réinitialisé ! Vous pouvez vous connecter.");

        } catch (Exception e) {
            showError("Erreur lors de la réinitialisation : " + e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }
}

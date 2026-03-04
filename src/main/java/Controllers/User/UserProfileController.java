package Controllers.User;

import Entities.User;
import Service.AuthService;
import Service.UserService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import util.SceneManager;
import util.Session;
import util.AvatarUtil;
import javafx.scene.layout.StackPane;
import Service.ImgBBService;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ResourceBundle;
import util.PasswordUtil;

public class UserProfileController implements Initializable {

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private ComboBox<String> genderBox;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField photoUrlField;

    @FXML
    private StackPane avatarPane;

    @FXML
    private Label initialsLabel;

    private User currentUser;
    private final UserService userService = new UserService();
    private final AuthService authService = AuthService.getInstance();
    private final ImgBBService imgBBService = ImgBBService.getInstance();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = Session.getInstance().getUser();

        genderBox.setItems(FXCollections.observableArrayList("Homme", "Femme", "Autre"));

        if (currentUser != null) {
            firstNameField.setText(currentUser.getFirstName());
            lastNameField.setText(currentUser.getLastName());
            emailField.setText(currentUser.getEmail());
            phoneField.setText(currentUser.getPhone());
            if (currentUser.getDateOfBirth() != null) {
                dobPicker.setValue(currentUser.getDateOfBirth().toLocalDate());
            }
            genderBox.setValue(currentUser.getGender() != null ? currentUser.getGender() : "Homme");

            updateAvatar();
        } else {
            showError("Erreur : Aucun utilisateur connecté.");
        }
    }

    @FXML
    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir une image de profil");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif"));

        File file = fileChooser.showOpenDialog(avatarPane.getScene().getWindow());
        if (file != null) {
            try {
                showSuccess("Téléchargement de l'image en cours...");

                // Upload vers ImgBB
                String imageUrl = imgBBService.uploadImage(file);

                currentUser.setPhotoUrl(imageUrl);
                updateAvatar();
                showSuccess("Photo mise à jour ! Cliquez sur Sauvegarder pour confirmer.");
            } catch (Exception e) {
                showError("Erreur lors de l'upload : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSaveChanges() {
        if (currentUser == null)
            return;

        String newFirstName = firstNameField.getText().trim();
        String newLastName = lastNameField.getText().trim();
        String newEmail = emailField.getText().trim();
        String newPhone = phoneField.getText().trim();
        LocalDate newDob = dobPicker.getValue();
        String newGender = genderBox.getValue();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()
                || newDob == null) {
            showError("Veuillez remplir les champs obligatoires (*).");
            return;
        }

        try {
            // Check if email changed and is already taken
            if (!newEmail.equals(currentUser.getEmail()) && userService.emailExists(newEmail)) {
                showError("Cet email est déjà utilisé par un autre compte.");
                return;
            }

            // Update user details
            currentUser.setFirstName(newFirstName);
            currentUser.setLastName(newLastName);
            currentUser.setEmail(newEmail);
            currentUser.setPhone(newPhone);
            currentUser.setDateOfBirth(Date.valueOf(newDob));
            currentUser.setGender(newGender);
            // photoUrl is already set in handleUploadAvatar

            // Handle Password Change if requested
            if (newPassword != null && !newPassword.isEmpty()) {
                if (!newPassword.equals(confirmPassword)) {
                    showError("Les mots de passe ne correspondent pas.");
                    return;
                }
                if (newPassword.length() < 6) {
                    showError("Le mot de passe doit contenir au moins 6 caractères.");
                    return;
                }
                // Update password via auth service
                authService.resetPassword(newEmail, newPassword);
                currentUser.setPassword(PasswordUtil.hashPassword(newPassword));
            }

            userService.update(currentUser);
            // Refresh session
            Session.getInstance().setUser(currentUser);

            updateAvatar();
            showSuccess("Profil mis à jour avec succès !");

        } catch (SQLException e) {
            showError("Erreur de base de données : " + e.getMessage());
        } catch (Exception e) {
            showError("Erreur lors de la mise à jour : " + e.getMessage());
        }
    }

    @FXML
    private void handleBackToDashboard() {
        SceneManager.loadPage("/com/example/psy/intro/dashboard.fxml");
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message);
    }

    private void showSuccess(String message) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText(message);
    }

    private void updateAvatar() {
        if (currentUser != null && avatarPane != null && initialsLabel != null) {
            AvatarUtil.setAvatar(avatarPane, initialsLabel, currentUser.getFirstName(), currentUser.getLastName(),
                    currentUser.getPhotoUrl());
        }
    }
}

package Controllers.User;

import Entities.User;
import Service.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import util.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class UserEditController implements Initializable {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;


    @FXML
    private ComboBox<String> roleComboBox;

    private User currentUser;

    // --------------------------------------------------
    // Initialize UI ONLY (never touch currentUser here)
    // --------------------------------------------------
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBox.setItems(
                FXCollections.observableArrayList("PATIENT", "ADMIN","DOCTOR")
        );
    }

    // --------------------------------------------------
    // Inject user from previous controller
    // --------------------------------------------------
    public void setUser(User user) {
        if (user == null) return;

        this.currentUser = user;
        fillForm();
    }

    // --------------------------------------------------
    // Fill form with user data
    // --------------------------------------------------
    private void fillForm() {
        fullNameField.setText(currentUser.getFullName());
        usernameField.setText(currentUser.getUsername());
        emailField.setText(currentUser.getEmail());
        roleComboBox.setValue(currentUser.getRole());
    }

    // --------------------------------------------------
    // Save changes
    // --------------------------------------------------
    @FXML
    private void handleSave(ActionEvent event) {

        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur sélectionné.");
            return;
        }

        String fullName = fullNameField.getText().trim();
        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Nom, nom d'utilisateur et email sont obligatoires.");
            return;
        }

        currentUser.setFullName(fullName);
        currentUser.setUsername(username);
        currentUser.setEmail(email);
        currentUser.setRole(role);



        try {
            UserService service = new UserService();
            service.update(currentUser);

            showAlert(Alert.AlertType.INFORMATION,
                    "Succès", "Utilisateur mis à jour avec succès.");
            SceneManager.switchScene("/com/example/psy/User/users.fxml");

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR,
                    "Erreur", "Échec de la mise à jour : " + e.getMessage());
        }
    }

    // --------------------------------------------------
    // Cancel / Close
    // --------------------------------------------------
    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        fullNameField.getScene().getWindow().hide();
    }

    // --------------------------------------------------
    // Utility
    // --------------------------------------------------
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
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
    private TextField emailField;

    @FXML
    private ComboBox<String> roleComboBox;

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBox.setItems(
                FXCollections.observableArrayList("patient", "admin"));
    }

    public void setUser(User user) {
        if (user == null)
            return;

        this.currentUser = user;
        fillForm();
    }

    private void fillForm() {
        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        roleComboBox.setValue(currentUser.getRole().toLowerCase());
    }

    @FXML
    private void handleSave(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun utilisateur sélectionné.");
            return;
        }

        String fullName = fullNameField.getText().trim();
        String email = emailField.getText().trim();
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation",
                    "Nom et email sont obligatoires.");
            return;
        }

        String[] names = fullName.split(" ", 2);
        currentUser.setFirstName(names[0]);
        currentUser.setLastName(names.length > 1 ? names[1] : "");
        currentUser.setEmail(email);
        currentUser.setRole(role.toLowerCase());

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

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        fullNameField.getScene().getWindow().hide();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
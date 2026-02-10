package Controllers.User;

import Entities.User;
import Service.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import util.SceneManager;

import java.sql.SQLException;
import java.util.Arrays;

public class UserAddController {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private Button addButton;

    @FXML
    public void initialize() {
        // définir les rôles disponibles (adapter si nécessaire)
        roleComboBox.setItems(FXCollections.observableArrayList(Arrays.asList("PATIENT", "ADMIN","DOCTOR")));
        roleComboBox.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        String fullName = fullNameField.getText() != null ? fullNameField.getText().trim() : "";
        String username = usernameField.getText() != null ? usernameField.getText().trim() : "";
        String email = emailField.getText() != null ? emailField.getText().trim() : "";
        String password = passwordField.getText() != null ? passwordField.getText() : "";
        String role = roleComboBox.getValue();

        if (fullName.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Veuillez remplir tous les champs obligatoires.");
            return;
        }

        User user = new User();
        user.setFullName(fullName);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);

        try {
            UserService service = new UserService();
            service.create(user);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur ajouté avec succès.");
            clearForm();
            SceneManager.switchScene("/com/example/psy/User/users.fxml");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec lors de l'ajout de l'utilisateur : " + e.getMessage());
        }
    }

    private void clearForm() {
        fullNameField.clear();
        usernameField.clear();
        emailField.clear();
        passwordField.clear();
        roleComboBox.getSelectionModel().selectFirst();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
}

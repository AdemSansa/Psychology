package Controllers.User;

import Entities.User;
import Service.UserService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import util.AvatarUtil;
import util.SceneManager;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class UserEditController implements Initializable {

    @FXML
    private TextField fullNameField;

    @FXML
    private TextField emailField;

    @FXML
    private TextField phoneField;

    @FXML
    private DatePicker dobPicker;

    @FXML
    private ComboBox<String> genderComboBox;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private StackPane avatarPane;

    @FXML
    private Label initialsLabel;

    private User currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roleComboBox.setItems(
                FXCollections.observableArrayList("patient", "admin"));
        genderComboBox.setItems(
                FXCollections.observableArrayList("Homme", "Femme", "Autre"));
    }

    public void setUser(User user) {
        if (user == null)
            return;
        this.currentUser = user;
        fillForm();
    }

    private void fillForm() {
        if (currentUser == null)
            return;

        fullNameField.setText(currentUser.getFullName());
        emailField.setText(currentUser.getEmail());
        phoneField.setText(currentUser.getPhone());

        if (currentUser.getDateOfBirth() != null) {
            dobPicker.setValue(currentUser.getDateOfBirth().toLocalDate());
        }

        genderComboBox.setValue(currentUser.getGender());

        if (currentUser.getRole() != null) {
            roleComboBox.setValue(currentUser.getRole().toLowerCase());
        }

        updateAvatar();
    }

    @FXML
    private void handleUploadAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Modifier l'image de profil");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(avatarPane.getScene().getWindow());
        if (file != null) {
            currentUser.setPhotoUrl(file.toURI().toString());
            updateAvatar();
        }
    }

    private void updateAvatar() {
        if (currentUser != null) {
            AvatarUtil.setAvatar(avatarPane, initialsLabel, currentUser.getFirstName(), currentUser.getLastName(),
                    currentUser.getPhotoUrl());
        }
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

        if (fullName.isEmpty() || email.isEmpty() || role == null) {
            showAlert(Alert.AlertType.WARNING, "Validation", "Les champs marqués d'une étoile (*) sont obligatoires.");
            return;
        }

        String[] names = fullName.split(" ", 2);
        currentUser.setFirstName(names[0]);
        currentUser.setLastName(names.length > 1 ? names[1] : "");
        currentUser.setEmail(email);
        currentUser.setRole(role.toLowerCase());
        currentUser.setPhone(phoneField.getText());
        if (dobPicker.getValue() != null) {
            currentUser.setDateOfBirth(Date.valueOf(dobPicker.getValue()));
        } else {
            currentUser.setDateOfBirth(null);
        }
        currentUser.setGender(genderComboBox.getValue());

        try {
            UserService service = new UserService();
            service.update(currentUser);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Utilisateur modifié avec succès.");
            SceneManager.switchScene("/com/example/psy/User/users.fxml");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        SceneManager.switchScene("/com/example/psy/User/users.fxml");
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

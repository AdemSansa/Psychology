package Controllers.auth;

import Entities.Therapistis;
import Entities.User;
import Service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import util.SceneManager;

public class RegisterController {
    @FXML
    private ComboBox<String> accountTypeBox;
    @FXML
    private TextField fullNameField;
    @FXML
    private TextField emailField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private VBox therapistFields;
    @FXML
    private TextField phoneField;
    @FXML
    private TextField specializationField;
    @FXML
    private Label messageLabel;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        accountTypeBox.setItems(FXCollections.observableArrayList("Patient", "Therapist"));
        accountTypeBox.setValue("Patient");
    }

    @FXML
    private void handleAccountTypeChange() {
        boolean isTherapist = "Therapist".equals(accountTypeBox.getValue());
        therapistFields.setVisible(isTherapist);
        therapistFields.setManaged(isTherapist);
    }

    @FXML
    private void handleRegister() {
        // Reset styles and message
        fullNameField.getStyleClass().remove("form-error");
        emailField.getStyleClass().remove("form-error");
        passwordField.getStyleClass().remove("form-error");
        phoneField.getStyleClass().remove("form-error");
        specializationField.getStyleClass().remove("form-error");
        messageLabel.setText("");

        try {
            boolean hasError = false;
            StringBuilder errorMsg = new StringBuilder();

            String accountType = accountTypeBox.getValue();
            String fullName = fullNameField.getText();

            if (fullName == null || fullName.trim().isEmpty()) {
                fullNameField.getStyleClass().add("form-error");
                errorMsg.append("Full name is required. ");
                hasError = true;
            }

            if (emailField.getText() == null || !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                emailField.getStyleClass().add("form-error");
                errorMsg.append("Invalid email address. ");
                hasError = true;
            }

            if (passwordField.getText() == null || passwordField.getText().length() < 4) {
                passwordField.getStyleClass().add("form-error");
                errorMsg.append("Password too short (min 4). ");
                hasError = true;
            }

            if ("Therapist".equals(accountType)) {
                if (phoneField.getText() == null || !phoneField.getText().matches("^[24579]\\d{7}$")) {
                    phoneField.getStyleClass().add("form-error");
                    errorMsg.append("Invalid Tunisian phone (8 digits). ");
                    hasError = true;
                }
                if (specializationField.getText() == null || specializationField.getText().trim().isEmpty()) {
                    specializationField.getStyleClass().add("form-error");
                    errorMsg.append("Specialization is required. ");
                    hasError = true;
                }
            }

            if (hasError) {
                throw new Exception(errorMsg.toString().trim());
            }

            String[] names = fullName.trim().split(" ", 2);
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : "";

            if ("Therapist".equals(accountType)) {
                Therapistis therapist = new Therapistis();
                therapist.setFirstName(firstName);
                therapist.setLastName(lastName);
                therapist.setEmail(emailField.getText());
                therapist.setPassword(passwordField.getText());
                therapist.setPhoneNumber(phoneField.getText());
                therapist.setSpecialization(specializationField.getText());
                therapist.setConsultationType("ONLINE"); // Use valid DB enum value
                therapist.setStatus("ACTIVE");

                authService.registerTherapist(therapist);
            } else {
                User user = new User(
                        firstName,
                        lastName,
                        emailField.getText(),
                        passwordField.getText());
                authService.register(user);
            }

            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Account created successfully ✔");
            SceneManager.switchScene("/com/example/psy/auth/login.fxml");

        } catch (Exception e) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void goToLogin() {
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }
}

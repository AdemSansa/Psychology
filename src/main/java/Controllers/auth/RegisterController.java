package Controllers.auth;

import Entities.Specialization;
import Entities.Therapistis;
import Entities.User;
import Service.AuthService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import util.DiplomaValidator;
import util.SceneManager;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    private ComboBox<String> specializationField;
    @FXML
    private TextField photoUrlField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;
    @FXML
    private Label messageLabel;

    // Diploma upload UI
    @FXML
    private Label diplomaFileLabel;
    @FXML
    private Label diplomaStatusLabel;

    /** Holds the last file selected by the user via the FileChooser. */
    private File selectedDiplomaFile = null;
    /** True only when a file has been chosen and DiplomaValidator approved it. */
    private boolean diplomaValid = false;

    private final AuthService authService = AuthService.getInstance();

    @FXML
    public void initialize() {
        accountTypeBox.setItems(FXCollections.observableArrayList("Patient", "Therapist"));
        accountTypeBox.setValue("Patient");

        // Populate specialization options
        for (Specialization s : Specialization.values()) {
            specializationField.getItems().add(s.getDisplayName());
        }
    }

    @FXML
    private void handleAccountTypeChange() {
        boolean isTherapist = "Therapist".equals(accountTypeBox.getValue());
        therapistFields.setVisible(isTherapist);
        therapistFields.setManaged(isTherapist);

        // Reset diploma state when switching account type
        selectedDiplomaFile = null;
        diplomaValid = false;
        if (diplomaFileLabel != null)
            diplomaFileLabel.setText("No file selected");
        if (diplomaStatusLabel != null)
            diplomaStatusLabel.setText("");
    }

    /**
     * Opens a FileChooser so the therapist can pick their diploma/certificate.
     * Immediately runs DiplomaValidator and updates the status labels.
     */
    @FXML
    private void handleDiplomaUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Diploma or Certificate");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported Files (PDF, TXT)", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"));

        File file = fileChooser.showOpenDialog(
                diplomaFileLabel.getScene() != null ? diplomaFileLabel.getScene().getWindow() : null);

        if (file == null) {
            // User cancelled the dialog — keep previous state
            return;
        }

        selectedDiplomaFile = file;
        diplomaFileLabel.setText(file.getName());
        diplomaFileLabel.setStyle("-fx-text-fill: #333; -fx-font-style: normal;");

        try {
            boolean valid = DiplomaValidator.validateDiploma(file);
            diplomaValid = valid;
            if (valid) {
                diplomaStatusLabel.setText("✔ Valid diploma — psychology keywords detected");
                diplomaStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-size: 12px; -fx-font-weight: bold;");
            } else {
                diplomaStatusLabel.setText("✘ Invalid — no psychology keywords found");
                diplomaStatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px; -fx-font-weight: bold;");
            }
        } catch (UnsupportedOperationException e) {
            diplomaValid = false;
            diplomaStatusLabel.setText("✘ Unsupported file type (use PDF or TXT)");
            diplomaStatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px; -fx-font-weight: bold;");
        } catch (Exception e) {
            diplomaValid = false;
            diplomaStatusLabel.setText("✘ Could not read file: " + e.getMessage());
            diplomaStatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-size: 12px; -fx-font-weight: bold;");
        }
    }

    @FXML
    private void handleLocationLookup() {
        String address = descriptionArea.getText();
        if (address == null || address.trim().isEmpty()) {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Veuillez saisir une adresse dans la description.");
            return;
        }

        new Thread(() -> {
            try {
                double[] coords = fetchCoordinates(address.trim());
                if (coords != null) {
                    javafx.application.Platform.runLater(() -> {
                        latitudeField.setText(String.valueOf(coords[0]));
                        longitudeField.setText(String.valueOf(coords[1]));
                        messageLabel.setStyle("-fx-text-fill: green;");
                        messageLabel.setText("Coordonnées trouvées pour: " + address);
                    });
                } else {
                    javafx.application.Platform.runLater(() -> {
                        messageLabel.setStyle("-fx-text-fill: red;");
                        messageLabel.setText("Lieu non trouvé dans la description.");
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private double[] fetchCoordinates(String locationName) throws Exception {
        // Try exact match first
        double[] result = performGeocoding(locationName);

        // Fallback: append ", Tunisia" if country not specified to help Nominatim
        if (result == null && !locationName.toLowerCase().contains("tunisi")) {
            result = performGeocoding(locationName + ", Tunisia");
        }

        return result;
    }

    private double[] performGeocoding(String query) throws Exception {
        String urlStr = "https://nominatim.openstreetmap.org/search?q="
                + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&format=json&limit=1";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        // Nominatim requires a descriptive User-Agent
        conn.setRequestProperty("User-Agent", "PsychologyApp/1.0 (Contact: psychiatric-app@esi.tn)");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() == 200) {
            try (InputStream is = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);
                if (root.isArray() && root.size() > 0) {
                    double lat = root.get(0).get("lat").asDouble();
                    double lon = root.get(0).get("lon").asDouble();
                    return new double[] { lat, lon };
                }
            }
        }
        return null;
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
                if (specializationField.getValue() == null || specializationField.getValue().trim().isEmpty()) {
                    specializationField.getStyleClass().add("form-error");
                    errorMsg.append("Specialization is required. ");
                    hasError = true;
                }

                // --- Diploma validation gate ---
                if (selectedDiplomaFile == null) {
                    errorMsg.append("Please upload your diploma or certificate. ");
                    hasError = true;
                } else if (!diplomaValid) {
                    errorMsg.append("Your diploma does not contain required psychology keywords. ");
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
                therapist.setSpecialization(specializationField.getValue());
                therapist.setConsultationType("ONLINE"); // Use valid DB enum value
                therapist.setStatus("ACTIVE");
                therapist.setDiplomaPath(selectedDiplomaFile.getAbsolutePath());
                therapist.setPhotoUrl(photoUrlField.getText());
                therapist.setDescription(descriptionArea.getText());

                try {
                    therapist.setLatitude(Double.parseDouble(latitudeField.getText()));
                    therapist.setLongitude(Double.parseDouble(longitudeField.getText()));
                } catch (Exception e) {
                    therapist.setLatitude(0);
                    therapist.setLongitude(0);
                }

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

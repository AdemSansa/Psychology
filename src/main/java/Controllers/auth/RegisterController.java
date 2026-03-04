package Controllers.auth;

import Entities.Specialization;
import Entities.Therapistis;
import Entities.User;
import Service.AuthService;
import Service.ImgBBService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import util.DiplomaValidator;
import util.SceneManager;

import com.github.sarxos.webcam.Webcam;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.util.concurrent.CompletableFuture;

public class RegisterController {

    // ================= BASIC FIELDS =================
    @FXML private ComboBox<String> accountTypeBox;
    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    // ================= PATIENT =================
    @FXML private VBox patientFields;
    @FXML private TextField phoneField;
    @FXML private DatePicker dobPicker;
    @FXML private ComboBox<String> genderBox;

    // ================= THERAPIST =================
    @FXML private VBox therapistFields;
    @FXML private TextField therapistPhoneField;
    @FXML private ComboBox<String> specializationField;

    @FXML private Label diplomaFileLabel;
    @FXML private Label diplomaStatusLabel;

    @FXML private TextArea descriptionArea;
    @FXML private TextField latitudeField;
    @FXML private TextField longitudeField;
    @FXML private TextField photoUrlField;

    // ================= FACE ID =================
    @FXML private ImageView facePreview;
    @FXML private Label faceStatusLabel;

    @FXML private Label messageLabel;

    // ================= VARIABLES =================
    private File selectedDiplomaFile;
    private boolean diplomaValid = false;
    private String capturedFaceUrl = null;

    private final AuthService authService = AuthService.getInstance();
    private final ImgBBService imgBBService = ImgBBService.getInstance();

    // ================= INITIALIZE =================
    @FXML
    public void initialize() {

        accountTypeBox.setItems(FXCollections.observableArrayList("Patient", "Therapist"));
        accountTypeBox.setValue("Patient");

        genderBox.setItems(FXCollections.observableArrayList("Homme", "Femme", "Autre"));
        genderBox.setValue("Homme");

        specializationField.setItems(FXCollections.observableArrayList());
        for (Specialization s : Specialization.values()) {
            specializationField.getItems().add(s.getDisplayName());
        }

        handleAccountTypeChange();
    }

    // ================= ACCOUNT TYPE SWITCH =================
    @FXML
    private void handleAccountTypeChange() {
        boolean isTherapist = "Therapist".equals(accountTypeBox.getValue());

        therapistFields.setVisible(isTherapist);
        therapistFields.setManaged(isTherapist);

        patientFields.setVisible(!isTherapist);
        patientFields.setManaged(!isTherapist);
    }

    // ================= DIPLOMA UPLOAD =================
    @FXML
    private void handleDiplomaUpload() {

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PDF or TXT", "*.pdf", "*.txt")
        );

        File file = fileChooser.showOpenDialog(diplomaFileLabel.getScene().getWindow());
        if (file == null) return;

        selectedDiplomaFile = file;
        diplomaFileLabel.setText(file.getName());

        try {
            diplomaValid = DiplomaValidator.validateDiploma(file);

            if (diplomaValid) {
                diplomaStatusLabel.setText("✔ Valid diploma");
                diplomaStatusLabel.setStyle("-fx-text-fill: green;");
            } else {
                diplomaStatusLabel.setText("✘ Invalid diploma");
                diplomaStatusLabel.setStyle("-fx-text-fill: red;");
            }

        } catch (Exception e) {
            diplomaValid = false;
            diplomaStatusLabel.setText("Error reading file");
            diplomaStatusLabel.setStyle("-fx-text-fill: red;");
        }
    }

    // ================= REGISTER =================
    @FXML
    private void handleRegister() {

        messageLabel.setText("");

        try {

            boolean hasError = false;
            StringBuilder errors = new StringBuilder();

            String accountType = accountTypeBox.getValue();
            String fullName = fullNameField.getText();

            if (fullName == null || fullName.trim().isEmpty()) {
                errors.append("Full name required. ");
                hasError = true;
            }

            if (emailField.getText() == null ||
                    !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                errors.append("Invalid email. ");
                hasError = true;
            }

            if (passwordField.getText() == null ||
                    passwordField.getText().length() < 4) {
                errors.append("Password too short. ");
                hasError = true;
            }

            if ("Therapist".equals(accountType)) {

                if (therapistPhoneField.getText() == null ||
                        !therapistPhoneField.getText().matches("^[24579]\\d{7}$")) {
                    errors.append("Invalid phone. ");
                    hasError = true;
                }

                if (specializationField.getValue() == null) {
                    errors.append("Specialization required. ");
                    hasError = true;
                }

                if (selectedDiplomaFile == null) {
                    errors.append("Upload diploma. ");
                    hasError = true;
                } else if (!diplomaValid) {
                    errors.append("Diploma invalid. ");
                    hasError = true;
                }

            } else {

                if (phoneField.getText() == null ||
                        !phoneField.getText().matches("^[24579]\\d{7}$")) {
                    errors.append("Invalid phone. ");
                    hasError = true;
                }

                if (dobPicker.getValue() == null) {
                    errors.append("Birth date required. ");
                    hasError = true;
                }
            }

            if (hasError) throw new Exception(errors.toString());

            String[] names = fullName.trim().split(" ", 2);
            String firstName = names[0];
            String lastName = names.length > 1 ? names[1] : "";

            if ("Therapist".equals(accountType)) {

                Therapistis therapist = new Therapistis();
                therapist.setFirstName(firstName);
                therapist.setLastName(lastName);
                therapist.setEmail(emailField.getText());
                therapist.setPassword(passwordField.getText());
                therapist.setPhoneNumber(therapistPhoneField.getText());
                therapist.setSpecialization(specializationField.getValue());
                therapist.setStatus("ACTIVE");
                therapist.setDiplomaPath(selectedDiplomaFile.getAbsolutePath());
                therapist.setDescription(descriptionArea.getText());

                if (capturedFaceUrl != null)
                    therapist.setPhotoUrl(capturedFaceUrl);
                System.out.println(therapist.getPhoneNumber());
                System.out.println(therapist.getPhotoUrl());
                authService.registerTherapist(therapist);

            } else {

                User user = new User(
                        firstName,
                        lastName,
                        emailField.getText(),
                        passwordField.getText()
                );

                user.setPhone(phoneField.getText());
                user.setDateOfBirth(Date.valueOf(dobPicker.getValue()));
                user.setGender(genderBox.getValue());

                if (capturedFaceUrl != null)
                    user.setPhotoUrl(capturedFaceUrl);
                System.out.println(user.getPhone());
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

    // ================= FACE CAPTURE =================
    @FXML
    private void handleCaptureFace() {

        faceStatusLabel.setText("Opening camera...");

        CompletableFuture.runAsync(() -> {
            try {

                Webcam webcam = Webcam.getDefault();
                if (webcam == null) {
                    updateFaceStatus("Camera not found", true);
                    return;
                }

                webcam.open();
                BufferedImage image = webcam.getImage();
                webcam.close();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                byte[] bytes = baos.toByteArray();

                String url = imgBBService.uploadImage(bytes);
                capturedFaceUrl = url;

                Platform.runLater(() -> {
                    facePreview.setImage(new Image(new ByteArrayInputStream(bytes)));
                    faceStatusLabel.setText("Face ID configured ✔");
                    faceStatusLabel.setStyle("-fx-text-fill: green;");
                });

            } catch (Exception e) {
                updateFaceStatus("Error: " + e.getMessage(), true);
            }
        });
    }

    private void updateFaceStatus(String msg, boolean error) {
        Platform.runLater(() -> {
            faceStatusLabel.setText(msg);
            faceStatusLabel.setStyle("-fx-text-fill:" + (error ? "red" : "blue"));
        });
    }

    // ================= LOCATION LOOKUP =================
    @FXML
    private void handleLocationLookup() {
        new Thread(() -> {
            try {
                String query = URLEncoder.encode(descriptionArea.getText(), StandardCharsets.UTF_8);
                String urlStr = "https://nominatim.openstreetmap.org/search?q=" +
                        query + "&format=json&limit=1";

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestProperty("User-Agent", "PsychologyApp");

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(conn.getInputStream());

                if (root.isArray() && root.size() > 0) {
                    double lat = root.get(0).get("lat").asDouble();
                    double lon = root.get(0).get("lon").asDouble();

                    Platform.runLater(() -> {
                        latitudeField.setText(String.valueOf(lat));
                        longitudeField.setText(String.valueOf(lon));
                    });
                }

            } catch (Exception ignored) {}
        }).start();
    }

    // ================= NAVIGATION =================
    @FXML
    private void goToLogin() {
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }
}
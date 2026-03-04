package Controllers.auth;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import Service.AuthService;
import Service.FaceIDService;
import Service.UserService;
import Service.TherapistService;
import Entities.User;
import Entities.Therapistis;
import util.PasswordUtil;
import util.SceneManager;
import util.Session;

import com.github.sarxos.webcam.Webcam;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    private final AuthService authService = AuthService.getInstance();
    private final FaceIDService faceIDService = FaceIDService.getInstance();
    private final UserService userService = new UserService();
    private final TherapistService therapistService = new TherapistService();

    @FXML
    private void handleLogin() {
        String identifier = usernameField.getText();
        String password = passwordField.getText();

        if (identifier.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill in all fields.");
            return;
        }

        try {
            String role = authService.login(identifier, password);
            handleSuccessfulLogin(role);
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void handleSuccessfulLogin(String role) {
        messageLabel.setStyle("-fx-text-fill: green;");
        messageLabel.setText("Connexion réussie ✔");

        // Role-based redirection
        switch (role.toLowerCase()) {
            case "admin":
            case "therapist":
            default:
                SceneManager.switchScene("/com/example/psy/intro/Home.fxml");
                break;
        }
    }

    @FXML
    private void handleFaceLogin() {
        String identifier = usernameField.getText().trim();

        if (identifier.isEmpty()) {
            showError("Veuillez entrer votre email pour le Face ID.");
            return;
        }

        messageLabel.setStyle("-fx-text-fill: blue;");
        messageLabel.setText("Recherche de l'utilisateur...");

        CompletableFuture.runAsync(() -> {
            try {
                // Find user and their reference photo
                String photoUrl = null;
                String role = null;
                User foundUser = userService.readByEmail(identifier);

                if (foundUser != null) {
                    photoUrl = foundUser.getPhotoUrl();
                    role = foundUser.getRole();
                } else {
                    Therapistis foundTherapist = therapistService.readByEmail(identifier);
                    if (foundTherapist != null) {
                        photoUrl = foundTherapist.getPhotoUrl();
                        role = "therapist";
                        // Prepare session user if matching
                    }
                }

                if (photoUrl == null || photoUrl.isEmpty()) {
                    updateMessageLater("Face ID non configuré (photo manquante).", true);
                    return;
                }

                String finalPhotoUrl = photoUrl;
                String finalRole = role;
                User sessionUser = foundUser; // fallback if therapist logic is separate

                updateMessageLater("Initialisation de la caméra...", false);

                // Capture image from webcam
                Webcam webcam = Webcam.getDefault();
                if (webcam == null) {
                    updateMessageLater("Caméra non trouvée.", true);
                    return;
                }

                webcam.open();
                BufferedImage image = webcam.getImage();
                webcam.close();

                if (image == null) {
                    updateMessageLater("Échec de la capture d'image.", true);
                    return;
                }

                updateMessageLater("Analyse du visage...", false);

                // Convert image to bytes
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(image, "jpg", baos);
                byte[] liveImageBytes = baos.toByteArray();

                // Compare faces via Face++
                double similarity = faceIDService.compareFaces(liveImageBytes, finalPhotoUrl);

                if (similarity >= 80.0) { // Threshold: 80%
                    // Login user into session (manually since we don't have password)
                    if (sessionUser == null) {
                        // It was a therapist, we need to convert to User for session if needed
                        Therapistis t = therapistService.readByEmail(identifier);
                        sessionUser = new User();
                        sessionUser.setId(t.getId());
                        sessionUser.setFirstName(t.getFirstName());
                        sessionUser.setLastName(t.getLastName());
                        sessionUser.setEmail(t.getEmail());
                        sessionUser.setRole("therapist");
                        sessionUser.setPhotoUrl(t.getPhotoUrl());
                    }

                    Session.getInstance().setUser(sessionUser);

                    Platform.runLater(() -> handleSuccessfulLogin(finalRole));
                } else {
                    updateMessageLater("Visage non reconnu (Score: " + (int) similarity + "%)", true);
                }

            } catch (Exception e) {
                e.printStackTrace();
                updateMessageLater("Erreur Face ID: " + e.getMessage(), true);
            }
        });
    }

    private void updateMessageLater(String text, boolean isError) {
        Platform.runLater(() -> {
            messageLabel.setText(text);
            messageLabel.setStyle("-fx-text-fill: " + (isError ? "red" : "blue") + ";");
        });
    }

    private void showError(String message) {
        messageLabel.setStyle("-fx-text-fill: red;");
        messageLabel.setText(message != null ? message : "Invalid login credentials.");
    }

    @FXML
    private void handleForgotPassword() {
        SceneManager.switchScene("/com/example/psy/auth/forgot_password.fxml");
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchScene("/com/example/psy/auth/register.fxml");
    }
}

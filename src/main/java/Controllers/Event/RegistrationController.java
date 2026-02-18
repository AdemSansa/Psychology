package Controllers.Event;

import Entities.Registration;
import Service.RegistrationService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import util.SceneManager;
import util.Session;

import java.sql.SQLException;
import java.util.UUID;

public class RegistrationController {

    @FXML private TextField searchField;
    @FXML private Label eventInfoLabel;
    @FXML private TextField nameField;
    @FXML private TextField emailField;     // NEW
    @FXML private TextField phoneField;     // NEW
    @FXML private TextArea notesArea;       // NEW
    @FXML private ComboBox<String> statusBox;
    @FXML private FlowPane cardContainer;
    @FXML private Label messageLabel;
    @FXML private Label nameError, emailError, phoneError;
    @FXML private VBox registrationsListContainer;
    @FXML private ScrollPane mainScrollPane;
    @FXML private Button submitBtn;

    private final RegistrationService service = new RegistrationService();

    private ObservableList<Registration> list = FXCollections.observableArrayList();
    private ObservableList<Registration> filtered = FXCollections.observableArrayList();

    private int eventId;
    private Registration selected = null;

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("registered", "attended", "cancelled");
        statusBox.setValue("registered");

        // Auto-fill name and email from session
        if (Session.getInstance().getUser() != null) {
            nameField.setText(Session.getInstance().getUser().getFirstName());

            // Try to get email from user object
            if (Session.getInstance().getUser() != null &&
                    Session.getInstance().getUser().getEmail() != null) {
                emailField.setText(Session.getInstance().getUser().getEmail());
            }
        }

        searchField.textProperty().addListener((obs, oldV, newV) -> filter());

        // Show registrations list only for Admin/Therapist
        String role = Session.getInstance().getUser().getRole().toLowerCase();
        if ("admin".equals(role) || "therapist".equals(role)) {
            registrationsListContainer.setVisible(true);
            registrationsListContainer.setManaged(true);
        }
    }

    public void setEventId(int id) {
        this.eventId = id;
        eventInfoLabel.setText("Register for Event ID: " + id);
        loadParticipants();
    }

    // ================= LOAD =================
    private void loadParticipants() {
        try {
            list.setAll(service.listByEvent(eventId));
            filtered.setAll(list);
            renderCards();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADD / UPDATE =================

    // ================= ADD / UPDATE =================
    @FXML
    private void handleRegister() {
        clearErrors();
        boolean isValid = true;

        try {
            String email = (emailField.getText() == null) ? "" : emailField.getText().trim();
            String phone = (phoneField.getText() == null) ? "" : phoneField.getText().trim();
            String name = (nameField.getText() == null) ? "" : nameField.getText().trim();

            // 1. Validation
            if (util.ValidationUtil.isEmpty(name)) {
                showError(nameError, "Participant name is required.");
                isValid = false;
            }

            if (!util.ValidationUtil.isValidEmail(email)) {
                showError(emailError, "Please enter a valid email address.");
                isValid = false;
            }

            if (!util.ValidationUtil.isValidPhone(phone)) {
                showError(phoneError, "Please enter a valid phone number (8-15 digits).");
                isValid = false;
            }

            if (!isValid) return;

            if (selected == null) {
                Registration r = new Registration();
                r.setEventId(eventId);
                r.setParticipantName(Session.getInstance().getUser().getFirstName());
                r.setParticipantEmail(email);
                r.setParticipantPhone(phone);
                r.setParticipantNotes(notesArea.getText().trim());
                r.setStatus(statusBox.getValue());
                r.setQrCode(UUID.randomUUID().toString());

                service.create(r);
                showMessage("✅ Registration successful!", "success");

                phoneField.clear();
                notesArea.clear();
            } else {
                String role2 = Session.getInstance().getUser().getRole();
                boolean isAdmin = "admin".equals(role2);
                boolean isOwner = selected.getParticipantName().equalsIgnoreCase(Session.getInstance().getUser().getFirstName());

                if (!isAdmin && !isOwner) {
                    util.ValidationUtil.showError("Permission Denied", "You cannot modify this registration.");
                    return;
                }

                selected.setParticipantName(nameField.getText() == null ? "" : nameField.getText().trim());
                selected.setParticipantEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                selected.setParticipantPhone(phoneField.getText() == null ? "" : phoneField.getText().trim());
                selected.setParticipantNotes(notesArea.getText() == null ? "" : notesArea.getText().trim());
                selected.setStatus(statusBox.getValue());
                service.update(selected);
                selected = null;
                submitBtn.setText("Submit Registration"); // Reset button text
                showMessage("✅ Registration updated!", "success");
            }

            loadParticipants();

        } catch (SQLException e) {
            util.ValidationUtil.showError("Database Error", "❌ " + e.getMessage());
        } catch (Exception e) {
            util.ValidationUtil.showError("System Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    // ================= CARD =================
    private VBox createCard(Registration r) {

        Label name = new Label("👤 " + r.getParticipantName());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        Label email = new Label("✉ " + (r.getParticipantEmail() != null ? r.getParticipantEmail() : "N/A"));
        email.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");

        Label phone = new Label("📞 " + (r.getParticipantPhone() != null ? r.getParticipantPhone() : "N/A"));
        phone.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");

        Label status = new Label("Status: " + r.getStatus());
        status.setStyle("-fx-text-fill: #718096; -fx-font-size: 12px;");

        // Buttons
        Button edit = new Button("Edit");
        Button delete = new Button("Delete");
        Button qrBtn = new Button("QR");

        // Styling buttons to match dashboard
        edit.getStyleClass().add("btn-primary"); // Brownish gradient
        edit.setPrefWidth(100);
        edit.setStyle("-fx-background-radius: 15;");

        delete.getStyleClass().add("btn-secondary"); // Grayish
        delete.setPrefWidth(100);
        delete.setStyle("-fx-background-radius: 8; -fx-border-color: #cbd5e0; -fx-text-fill: #4a5568;");

        qrBtn.getStyleClass().add("btn-secondary");
        qrBtn.setPrefWidth(85);
        qrBtn.setStyle("-fx-background-radius: 15; -fx-border-color: #d2b48c; -fx-text-fill: #6f4e37; -fx-font-weight: bold;");

        String role3 = Session.getInstance().getUser().getRole();
        boolean isAdmin = "admin".equals(role3);
        boolean isOwner = r.getParticipantName()
                .equalsIgnoreCase(Session.getInstance().getUser().getFirstName());

        // 🔐 SECURITY: ONLY Admin or Owner can See Edit/Delete
        if (!isAdmin && !isOwner) {
            edit.setVisible(false);
            edit.setManaged(false);
            delete.setVisible(false);
            delete.setManaged(false);
        }

        edit.setOnAction(e -> {
            selected = r;
            nameField.setText(r.getParticipantName());
            emailField.setText(r.getParticipantEmail());
            phoneField.setText(r.getParticipantPhone());
            notesArea.setText(r.getParticipantNotes());
            statusBox.setValue(r.getStatus());
            
            // 🔄 VISUAL FEEDBACK: Change button text and scroll up
            if (submitBtn != null) submitBtn.setText("Update Registration");
            if (mainScrollPane != null) mainScrollPane.setVvalue(0); // Scroll to top
        });

        delete.setOnAction(e -> {
            try {
                service.delete(r.getIdRegistration());
                loadParticipants();
                showMessage("✅ Registration deleted", "success");
            } catch (SQLException ex) {
                showMessage("❌ " + ex.getMessage(), "error");
            }
        });

        qrBtn.setOnAction(e -> showQRPopup(r.getQrCode()));

        HBox btnRow = new HBox(12, edit, delete, qrBtn);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        btnRow.setPadding(new javafx.geometry.Insets(10, 0, 0, 0));

        VBox card = new VBox(12, name, email, phone, status, btnRow);
        card.setPrefWidth(450);
        card.setStyle("-fx-background-color: #f7fafc; -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 0);");

        return card;
    }

    private void renderCards() {
        cardContainer.getChildren().clear();
        for (Registration r : filtered) {
            cardContainer.getChildren().add(createCard(r));
        }
    }

    // ================= FILTER =================
    private void filter() {
        String q = searchField.getText().toLowerCase().trim();

        if (q.isEmpty()) {
            filtered.setAll(list);
        } else {
            filtered.setAll(
                    list.stream()
                            .filter(r -> r.getParticipantName()
                                    .toLowerCase().contains(q) ||
                                    (r.getParticipantEmail() != null && r.getParticipantEmail().toLowerCase().contains(q)))
                            .toList()
            );
        }

        renderCards();
    }

    // ================= QR POPUP =================
    private void showQRPopup(String text) {

        Label title = new Label("QR Code");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        TextArea qrText = new TextArea(text);
        qrText.setEditable(false);
        qrText.setWrapText(true);

        Button close = new Button("Close");
        close.getStyleClass().add("btn-primary");

        VBox box = new VBox(15, title, qrText, close);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setStyle("-fx-padding:20;");

        Stage stage = new Stage();
        stage.setTitle("QR Code");

        close.setOnAction(e -> stage.close());

        stage.setScene(new Scene(box, 320, 220));
        stage.show();
    }

    private void showMessage(String msg, String type) {
        if (messageLabel != null) {
            messageLabel.setText(msg);
            messageLabel.setStyle(type.equals("success")
                    ? "-fx-text-fill: green; -fx-font-weight: bold;"
                    : "-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            // Fallback to alert
            Alert a = new Alert(Alert.AlertType.INFORMATION);
            a.setContentText(msg);
            a.showAndWait();
        }
    }

    private void clearErrors() {
        if (nameError != null) {
            nameError.setVisible(false);
            nameError.setManaged(false);
        }
        if (emailError != null) {
            emailError.setVisible(false);
            emailError.setManaged(false);
        }
        if (phoneError != null) {
            phoneError.setVisible(false);
            phoneError.setManaged(false);
        }
    }

    private void showError(Label label, String message) {
        if (label != null) {
            label.setText(message);
            label.setVisible(true);
            label.setManaged(true);
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.loadPage("/com/example/psy/Event/events.fxml");
    }
}

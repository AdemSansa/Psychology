package Controllers.Event;
import Entities.Event;
import Service.EventService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.SceneManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import javafx.stage.FileChooser;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;


public class EventAddController {



    // ===== FXML Fields =====
    @FXML private Label imagePathLabel;
    @FXML private ImageView imagePreview;
    private String savedImagePath = "";

    @FXML private TextField titleField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> typeComboBox;

    @FXML private DatePicker dateStartPicker;
    @FXML private TextField timeStartField;

    @FXML private DatePicker dateEndPicker;
    @FXML private TextField timeEndField;

    @FXML private TextField locationField;
    @FXML private TextField maxParticipantsField;
    @FXML private ComboBox<String> statusComboBox;

    // ===== Service =====
    private final EventService eventService = new EventService();

    @FXML private Label titleError, dateStartError, dateEndError, maxParticipantsError, descriptionError, locationError, typeError, statusError;

    // ===== Initialize =====
    @FXML
    public void initialize() {
        // Default values
        typeComboBox.getSelectionModel().selectFirst();
        statusComboBox.getSelectionModel().select("draft");
    }

    @FXML
    private void handleBrowseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Event Image");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File file = fileChooser.showOpenDialog(imagePathLabel.getScene().getWindow());
        if (file != null) {
            try {
                // Prepare directory
                File uploadDir = new File("uploads/events");
                if (!uploadDir.exists()) uploadDir.mkdirs();

                // Generate unique name
                String extension = "";
                int i = file.getName().lastIndexOf('.');
                if (i > 0) extension = file.getName().substring(i);

                String fileName = UUID.randomUUID().toString() + extension;
                File destFile = new File(uploadDir, fileName);

                // Copy file
                Files.copy(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                // Update UI
                savedImagePath = "uploads/events/" + fileName;
                imagePathLabel.setText(file.getName());

                Image img = new Image(destFile.toURI().toString());
                imagePreview.setImage(img);
                imagePreview.setVisible(true);
                imagePreview.setManaged(true);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ===== Add Event =====
    @FXML
    private void handleAddEvent() {
        clearErrors();
        boolean isValid = true;

        try {
            // 1. Basic Field Validation
            String title = titleField.getText() != null ? titleField.getText().trim() : "";
            if (util.ValidationUtil.isEmpty(title)) {
                showError(titleError, "Event title is required.");
                isValid = false;
            } else if (title.length() < 5) {
                showError(titleError, "Event title must be at least 5 characters.");
                isValid = false;
            }

            if (dateStartPicker.getValue() == null) {
                showError(dateStartError, "Start date is required.");
                isValid = false;
            }
            if (dateEndPicker.getValue() == null) {
                showError(dateEndError, "End date is required.");
                isValid = false;
            }

            // Description Validation
            String description = descriptionField.getText() != null ? descriptionField.getText().trim() : "";
            if (util.ValidationUtil.isEmpty(description)) {
                showError(descriptionError, "Description is required.");
                isValid = false;
            } else if (description.length() < 10) {
                showError(descriptionError, "Description must be at least 10 characters.");
                isValid = false;
            }

            // Location Validation
            String location = locationField.getText() != null ? locationField.getText().trim() : "";
            if (util.ValidationUtil.isEmpty(location)) {
                showError(locationError, "Location is required.");
                isValid = false;
            } else if (location.length() < 3) {
                showError(locationError, "Location must be at least 3 characters.");
                isValid = false;
            }

            // ComboBox Validation
            if (typeComboBox.getValue() == null) {
                showError(typeError, "Event type is required.");
                isValid = false;
            }
            if (statusComboBox.getValue() == null) {
                showError(statusError, "Status is required.");
                isValid = false;
            }

            if (!isValid) return;

            // 2. Date Validation
            LocalDateTime startDateTime = combineDateAndTime(dateStartPicker.getValue(), timeStartField.getText());
            LocalDateTime endDateTime = combineDateAndTime(dateEndPicker.getValue(), timeEndField.getText());

            if (startDateTime != null && startDateTime.isBefore(LocalDateTime.now())) {
                showError(dateStartError, "Start date/time cannot be in the past.");
                isValid = false;
            }

            if (!util.ValidationUtil.isAfter(endDateTime, startDateTime)) {
                showError(dateEndError, "End date/time must be after the start date/time.");
                isValid = false;
            }

            // 3. Max Participants Validation
            String maxParticipants = maxParticipantsField.getText();
            if (util.ValidationUtil.isEmpty(maxParticipants)) {
                showError(maxParticipantsError, "Max participants is required.");
                isValid = false;
            } else {
                try {
                    int val = Integer.parseInt(maxParticipants);
                    if (val <= 0 || val > 1000) {
                        showError(maxParticipantsError, "Must be between 1 and 1000.");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    showError(maxParticipantsError, "Must be a valid number.");
                    isValid = false;
                }
            }

            if (!isValid) return;

            // Create Event
            Event event = new Event();
            event.setImageUrl(savedImagePath);
            event.setTitle(titleField.getText().trim());
            event.setDescription(descriptionField.getText());
            event.setType(typeComboBox.getValue());
            event.setDateStart(startDateTime);
            event.setDateEnd(endDateTime);
            event.setLocation(locationField.getText());
            event.setMaxParticipants(Integer.parseInt(maxParticipantsField.getText()));
            event.setStatus(statusComboBox.getValue());
            event.setCreatedAt(LocalDateTime.now());

            // Set organizer as logged-in user
            if (util.Session.getInstance().getUser()!=null) {
                event.setOrganizerId(util.Session.getInstance().getUser().getId());
            } else {
                util.ValidationUtil.showError("Authentication Error", "You must be logged in to create an event.");
                return;
            }

            // Save
            eventService.create(event);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Event added successfully!");
            SceneManager.switchScene("/com/example/psy/Event/events.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            util.ValidationUtil.showError("System Error", "An unexpected error occurred: " + e.getMessage());
        }
    }

    // ===== Cancel =====
    @FXML
    private void handleCancel() {
        SceneManager.switchScene("/com/example/psy/Event/events.fxml");
    }

    // ===== Helpers =====
    private LocalDateTime combineDateAndTime(LocalDate date, String timeText) {
        LocalTime time;

        if (timeText == null || timeText.isEmpty()) {
            time = LocalTime.of(0, 0);
        } else {
            time = LocalTime.parse(timeText); // expects HH:mm
        }

        return LocalDateTime.of(date, time);
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }

    private void clearErrors() {
        titleError.setVisible(false);
        titleError.setManaged(false);
        dateStartError.setVisible(false);
        dateStartError.setManaged(false);
        dateEndError.setVisible(false);
        dateEndError.setManaged(false);
        maxParticipantsError.setVisible(false);
        maxParticipantsError.setManaged(false);
        descriptionError.setVisible(false);
        descriptionError.setManaged(false);
        locationError.setVisible(false);
        locationError.setManaged(false);
        typeError.setVisible(false);
        typeError.setManaged(false);
        statusError.setVisible(false);
        statusError.setManaged(false);
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

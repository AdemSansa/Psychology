package Controllers.Event;

import Entities.Event;
import Service.EventService;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import util.SceneManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventAddController {


    // ===== FXML Fields =====
    @FXML private TextField imageUrlField;

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

    // ===== Initialize =====
    @FXML
    public void initialize() {
        // Default values
        typeComboBox.getSelectionModel().selectFirst();
        statusComboBox.getSelectionModel().select("draft");
    }

    // ===== Add Event =====
    @FXML
    private void handleAddEvent() {
        try {
            // Validation
            if (titleField.getText().isEmpty()
                    || dateStartPicker.getValue() == null
                    || dateEndPicker.getValue() == null) {

                showAlert(Alert.AlertType.ERROR, "Validation Error",
                        "Title and dates are required.");
                return;
            }

            // Parse date & time
            LocalDateTime startDateTime = combineDateAndTime(
                    dateStartPicker.getValue(),
                    timeStartField.getText()
            );

            LocalDateTime endDateTime = combineDateAndTime(
                    dateEndPicker.getValue(),
                    timeEndField.getText()
            );

            if (endDateTime.isBefore(startDateTime)) {
                showAlert(Alert.AlertType.ERROR, "Validation Error",
                        "End date must be after start date.");
                return;
            }

            // Create Event
            Event event = new Event();
            event.setImageUrl(imageUrlField.getText());
            event.setTitle(titleField.getText());
            event.setDescription(descriptionField.getText());
            event.setType(typeComboBox.getValue());
            event.setDateStart(startDateTime);
            event.setDateEnd(endDateTime);
            event.setLocation(locationField.getText());
            event.setMaxParticipants(
                    Integer.parseInt(maxParticipantsField.getText())
            );
            event.setStatus(statusComboBox.getValue());

            // Auto fields
            event.setCreatedAt(LocalDateTime.now());
            event.setOrganizerId(1); // TODO: replace with logged-in user

            // Save
            eventService.create(event);

            showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Event added successfully!");

            SceneManager.switchScene("/com/example/psy/Event/events.fxml");

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Invalid Number",
                    "Max participants must be a number.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error",
                    "Something went wrong while saving the event.");
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

}

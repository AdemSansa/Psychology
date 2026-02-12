package Controllers.Event;

import Entities.Event;
import Service.EventService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import util.SceneManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventEditController {

    private Event CurrentEvent;

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

    @FXML private TextField imageUrlField;
    @FXML private ImageView eventImageView;

    EventService eventService = new EventService();

    public void setEvent(Event event) {
        if (event == null) return;
        this.CurrentEvent = event;
        fillFields();
    }

    private void fillFields() {
        titleField.setText(CurrentEvent.getTitle());
        descriptionField.setText(CurrentEvent.getDescription());
        typeComboBox.setValue(CurrentEvent.getType());
        dateStartPicker.setValue(CurrentEvent.getDateStart().toLocalDate());
        timeStartField.setText(CurrentEvent.getDateStart().toLocalTime().toString());
        dateEndPicker.setValue(CurrentEvent.getDateEnd().toLocalDate());
        timeEndField.setText(CurrentEvent.getDateEnd().toLocalTime().toString());
        locationField.setText(CurrentEvent.getLocation());
        maxParticipantsField.setText(String.valueOf(CurrentEvent.getMaxParticipants()));
        statusComboBox.setValue(CurrentEvent.getStatus());
        imageUrlField.setText(CurrentEvent.getImageUrl());
    }

    @FXML
    private void handlePreviewImage() {
        try {
            String url = imageUrlField.getText();

            if (url == null || url.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Enter image URL.");
                return;
            }

            Image img = new Image(url, true);
            if (img.isError()) {
                showAlert(Alert.AlertType.ERROR, "Error", "Invalid image URL.");
                return;
            }

            eventImageView.setImage(img);

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Cannot load image.");
        }
    }

    @FXML
    private void handleUpdateEvent() {
        try {
            LocalDateTime start = combineDateAndTime(dateStartPicker.getValue(), timeStartField.getText());
            LocalDateTime end = combineDateAndTime(dateEndPicker.getValue(), timeEndField.getText());

            if (end.isBefore(start)) {
                showAlert(Alert.AlertType.ERROR, "Validation", "End must be after start.");
                return;
            }

            CurrentEvent.setTitle(titleField.getText());
            CurrentEvent.setDescription(descriptionField.getText());
            CurrentEvent.setType(typeComboBox.getValue());
            CurrentEvent.setDateStart(start);
            CurrentEvent.setDateEnd(end);
            CurrentEvent.setLocation(locationField.getText());
            CurrentEvent.setMaxParticipants(Integer.parseInt(maxParticipantsField.getText()));
            CurrentEvent.setStatus(statusComboBox.getValue());
            CurrentEvent.setImageUrl(imageUrlField.getText());


            eventService.update(CurrentEvent);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated.");
            SceneManager.switchScene("/com/example/psy/Event/events.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Update failed.");
        }
    }

    @FXML
    private void handleCancel() {
        SceneManager.switchScene("/com/example/psy/Event/events.fxml");
    }

    private LocalDateTime combineDateAndTime(LocalDate date, String time) {
        LocalTime t = (time == null || time.isEmpty()) ? LocalTime.of(0, 0) : LocalTime.parse(time);
        return LocalDateTime.of(date, t);
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}

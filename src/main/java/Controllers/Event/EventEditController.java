package Controllers.Event;

import Entities.Event;
import Service.EventService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
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

    @FXML private Label imagePathLabel;
    @FXML private ImageView eventImageView;
    private String savedImagePath = "";

    @FXML private Label titleError, dateStartError, dateEndError, maxParticipantsError, descriptionError, locationError, typeError, statusError;
    private final EventService eventService = new EventService();

    public void setEvent(Event event) {
        if (event == null) return;
        this.CurrentEvent = event;
        fillFields();
    }

    // ===== 📍 Map Picker =====
    @FXML
    private void handlePickLocation() {
        openMapPicker(locationField);
    }

    private void openMapPicker(TextField targetField) {
        Stage mapStage = new Stage();
        mapStage.setTitle("📍 Pick Location");
        mapStage.initModality(Modality.APPLICATION_MODAL);

        WebView webView = new WebView();
        WebEngine engine = webView.getEngine();

        String currentVal = targetField.getText() != null ? targetField.getText().trim() : "";
        engine.loadContent(buildMapHtml(currentVal));

        Label hint = new Label("🖱️ Click to select, then confirm.");
        hint.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

        Button confirmBtn = new Button("Confirm Selection");
        confirmBtn.getStyleClass().add("btn-primary");
        confirmBtn.setStyle("-fx-font-size: 13px; -fx-padding: 6 15;");
        confirmBtn.setOnAction(e -> {
            try {
                Object result = engine.executeScript("selectedAddress");
                if (result instanceof String && !((String) result).isEmpty()) {
                    targetField.setText((String) result);
                    mapStage.close();
                } else {
                    util.ValidationUtil.showError("Selection Required", "Please click on the map to select a location first.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> mapStage.close());

        HBox bottom = new HBox(15, hint, confirmBtn, cancelBtn);
        bottom.setAlignment(Pos.CENTER_LEFT);
        bottom.setStyle("-fx-padding: 10 15; -fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-width: 1 0 0 0;");

        VBox root = new VBox(webView, bottom);
        VBox.setVgrow(webView, javafx.scene.layout.Priority.ALWAYS);

        mapStage.setScene(new Scene(root, 850, 600));
        mapStage.show();
    }

    private String buildMapHtml(String initialLocation) {
        return "<!DOCTYPE html><html><head>" +
               "<meta charset='utf-8'>" +
               "<link rel='stylesheet' href='https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'/>" +
               "<script src='https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'></script>" +
               "<style>html,body,#map{width:100%;height:100%;margin:0;padding:0;}</style>" +
               "</head><body>" +
               "<div id='map'></div>" +
               "<script>" +
               "var selectedAddress = '" + initialLocation.replace("'", "\\'") + "';" +
               "var map = L.map('map').setView([36.8, 10.2], 6);" +
               "L.tileLayer('https://tile.openstreetmap.org/{z}/{x}/{y}.png',{" +
               "  maxZoom:19," +
               "  attribution:'&copy; OpenStreetMap contributors'" +
               "}).addTo(map);" +
               "var marker = null;" +
               "function updateMarker(lat, lon, addr) {" +
               "  if(marker) map.removeLayer(marker);" +
               "  marker = L.marker([lat,lon]).addTo(map);" +
               "  marker.bindPopup('<b>'+addr+'</b>').openPopup();" +
               "  selectedAddress = addr;" +
               "}" +
               "map.on('click', function(e){" +
               "  var lat = e.latlng.lat; var lon = e.latlng.lng;" +
               "  if(marker) map.removeLayer(marker);" +
               "  marker = L.marker([lat,lon]).addTo(map);" +
               "  marker.bindPopup('\u23f3 Fetching address...').openPopup();" +
               "  fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat='+lat+'&lon='+lon," +
               "    {headers:{'User-Agent':'PsychologyApp/1.0'}})" +
               "  .then(r=>r.json())" +
               "  .then(data=>{" +
               "    var addr = data.display_name || (lat.toFixed(5)+', '+lon.toFixed(5));" +
               "    updateMarker(lat, lon, addr);" +
               "  }).catch(function(){" +
               "    var f = lat.toFixed(5)+', '+lon.toFixed(5);" +
               "    updateMarker(lat, lon, f);" +
               "  });" +
               "});" +
               "if(selectedAddress && selectedAddress.length > 3) {" +
               "  fetch('https://nominatim.openstreetmap.org/search?format=json&q='+encodeURIComponent(selectedAddress))" +
               "  .then(r=>r.json())" +
               "  .then(data=>{" +
               "    if(data && data.length > 0) {" +
               "      var lat=data[0].lat; var lon=data[0].lon;" +
               "      map.setView([lat, lon], 13);" +
               "      updateMarker(lat, lon, selectedAddress);" +
               "    }" +
               "  });" +
               "}" +
               "</script></body></html>";
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

        savedImagePath = CurrentEvent.getImageUrl();
        if (savedImagePath != null && !savedImagePath.isEmpty()) {
            imagePathLabel.setText("Current Image");
            try {
                // Determine if it's a URL or a local file
                String imageUri = savedImagePath.startsWith("http") ? savedImagePath : new File(savedImagePath).toURI().toString();
                eventImageView.setImage(new Image(imageUri, true));
            } catch (Exception e) {
                System.err.println("Failed to load existing image: " + e.getMessage());
            }
        }
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
                eventImageView.setImage(img);

            } catch (IOException e) {
                e.printStackTrace();
                util.ValidationUtil.showError("Upload Error", "Failed to copy image: " + e.getMessage());
            }
        }
    }



    @FXML
    private void handleUpdateEvent() {
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
            LocalDateTime start = combineDateAndTime(dateStartPicker.getValue(), timeStartField.getText());
            LocalDateTime end = combineDateAndTime(dateEndPicker.getValue(), timeEndField.getText());

            if (start != null && start.isBefore(LocalDateTime.now())) {
                // If it's a new date being set in the past, or if the original date was in the past and not changed,
                // we might want to allow it only if it's unchanged. But for simplicity, let's enforce future dates for any edit.
                showError(dateStartError, "Start date/time cannot be in the past.");
                isValid = false;
            }

            if (!util.ValidationUtil.isAfter(end, start)) {
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

            CurrentEvent.setTitle(titleField.getText().trim());
            CurrentEvent.setDescription(descriptionField.getText());
            CurrentEvent.setType(typeComboBox.getValue());
            CurrentEvent.setDateStart(start);
            CurrentEvent.setDateEnd(end);
            CurrentEvent.setLocation(locationField.getText());
            CurrentEvent.setMaxParticipants(Integer.parseInt(maxParticipantsField.getText()));
            CurrentEvent.setStatus(statusComboBox.getValue());
            CurrentEvent.setImageUrl(savedImagePath);

            eventService.update(CurrentEvent);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Event updated successfully.");
            SceneManager.switchScene("/com/example/psy/Event/events.fxml");

        } catch (Exception e) {
            e.printStackTrace();
            util.ValidationUtil.showError("Update Failed", "An error occurred while updating the event: " + e.getMessage());
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

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
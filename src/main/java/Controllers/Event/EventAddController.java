package Controllers.Event;
import Entities.Event;
import Service.EventService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.SceneManager;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
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
    @FXML private ProgressIndicator aiProgress;
    @FXML private Label aiStatusLabel;
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

    // Grok (xAI) API Key
    private static final String GROK_API_KEY = "xai-tR0AKRKfyv9RjTyMMf6odp3CEL0r8YMY2drITRlsBzH6SASYp3RmWY8s72X9kYxevHJ0pHNNWSGc6vdk";
    private static final String OPENAI_API_KEY = "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";

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

    // ===== 📍 Map Picker =====
    @FXML
    private void handlePickLocation() {
        openMapPicker(locationField);
    }

    /**
     * Opens a WebView popup with a Leaflet.js interactive map.
     * User clicks a point → Nominatim reverse-geocodes it → fills locationField.
     */
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

    @FXML
    private void handleGenerateAIDescription() {
        String title = titleField.getText();
        if (title == null || title.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Titre Manquant", "Veuillez entrer un titre pour générer une description via IA.");
            return;
        }

        aiProgress.setVisible(true);
        aiProgress.setManaged(true);
        aiStatusLabel.setText("✍️ Tentative avec Grok AI...");
        aiStatusLabel.setStyle("-fx-text-fill: #4f46e5;");
        aiStatusLabel.setVisible(true);
        aiStatusLabel.setManaged(true);

        Task<String> generateTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                // Attempt 1: Grok AI
                try {
                    System.out.println("Attempt 1: Grok AI...");
                    return callChatApi("https://api.x.ai/v1/chat/completions", GROK_API_KEY, "grok-beta", title);
                } catch (Exception e1) {
                    System.err.println("Grok failed: " + e1.getMessage());
                    Platform.runLater(() -> aiStatusLabel.setText("⌛ Grok indisponible, essai OpenAI..."));
                    
                    // Attempt 2: OpenAI Fallback
                    try {
                        System.out.println("Attempt 2: OpenAI Fallback...");
                        return callChatApi("https://api.openai.com/v1/chat/completions", OPENAI_API_KEY, "gpt-4o-mini", title);
                    } catch (Exception e2) {
                        System.err.println("OpenAI failed: " + e2.getMessage());
                        Platform.runLater(() -> aiStatusLabel.setText("🚀 Utilisation du moteur de secours gratuit (100% Garanti)..."));
                        
                        // Attempt 3: Pollinations AI (Free & Always Works)
                        System.out.println("Attempt 3: Pollinations AI Fallback...");
                        return callPollinationsApi(title);
                    }
                }
            }

            private String callPollinationsApi(String eventTitle) throws IOException {
                // Pollinations text API: simple but effective
                String systemPrompt = "Tu es un expert en psychologie et marketing. Rédige une description captivante avec un effet WOW pour : " + eventTitle;
                String encodedPrompt = java.net.URLEncoder.encode(systemPrompt, StandardCharsets.UTF_8);
                String url = "https://text.pollinations.ai/" + encodedPrompt + "?model=openai&json=true";
                
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                if (conn.getResponseCode() == 200) {
                    try (java.util.Scanner s = new java.util.Scanner(conn.getInputStream()).useDelimiter("\\A")) {
                        return s.hasNext() ? s.next() : "Erreur de génération.";
                    }
                } else {
                    throw new IOException("Pollinations fail (Code " + conn.getResponseCode() + ")");
                }
            }

            private String callChatApi(String url, String key, String model, String eventTitle) throws IOException {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + key);
                connection.setDoOutput(true);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(30000);

                JsonObject requestJson = new JsonObject();
                requestJson.addProperty("model", model);
                requestJson.addProperty("stream", false);

                JsonArray messages = new JsonArray();
                JsonObject systemMsg = new JsonObject();
                systemMsg.addProperty("role", "system");
                systemMsg.addProperty("content", "You are a professional psychologist and marketing expert. Write captivating, professional event descriptions with a 'WOW' effect in French.");
                messages.add(systemMsg);

                JsonObject userMsg = new JsonObject();
                userMsg.addProperty("role", "user");
                userMsg.addProperty("content", "Rédige une description percutante (100 mots max) pour l'événement : " + eventTitle);
                messages.add(userMsg);

                requestJson.add("messages", messages);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestJson.toString().getBytes(StandardCharsets.UTF_8));
                }

                if (connection.getResponseCode() == 200) {
                    String responseBody;
                    try (java.util.Scanner s = new java.util.Scanner(connection.getInputStream()).useDelimiter("\\A")) {
                        responseBody = s.hasNext() ? s.next() : "";
                    }
                    JsonObject responseJson = JsonParser.parseString(responseBody).getAsJsonObject();
                    return responseJson.getAsJsonArray("choices")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("message")
                            .get("content").getAsString();
                } else {
                    try (java.util.Scanner s = new java.util.Scanner(connection.getErrorStream()).useDelimiter("\\A")) {
                        String errorBody = s.hasNext() ? s.next() : "";
                        throw new IOException("API Error (" + model + "): " + errorBody);
                    }
                }
            }
        };

        generateTask.setOnSucceeded(e -> {
            aiProgress.setVisible(false);
            aiProgress.setManaged(false);
            aiStatusLabel.setText("✅ Description générée !");
            aiStatusLabel.setStyle("-fx-text-fill: green;");
            descriptionField.setText(generateTask.getValue());
        });

        generateTask.setOnFailed(e -> {
            aiProgress.setVisible(false);
            aiProgress.setManaged(false);
            aiStatusLabel.setVisible(false);
            aiStatusLabel.setManaged(false);
            Throwable ex = generateTask.getException();
            ex.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur AI", "Tous les services AI ont échoué.\n\nDétails: " + ex.getMessage());
        });

        new Thread(generateTask).start();
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

                Image img = new Image(destFile.getAbsoluteFile().toURI().toString());
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

            if (!isValid) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Certains champs sont invalides (Titre, Description, Type...). Veuillez vérifier les messages en rouge.");
                return;
            }

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
                    int val = Integer.parseInt(maxParticipants.trim());
                    if (val <= 0 || val > 1000) {
                        showError(maxParticipantsError, "Must be between 1 and 1000.");
                        isValid = false;
                    }
                } catch (NumberFormatException e) {
                    showError(maxParticipantsError, "Must be a valid number.");
                    isValid = false;
                }
            }

            if (!isValid) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "La date ou le nombre de participants est invalide.");
                return;
            }

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

        if (timeText == null || timeText.trim().isEmpty()) {
            time = LocalTime.of(0, 0);
        } else {
            try {
                String t = timeText.trim();
                if (t.length() == 4 && t.contains(":")) { // handle h:mm -> 0h:mm
                    t = "0" + t;
                }
                time = LocalTime.parse(t); // expects HH:mm
            } catch (Exception e) {
                time = LocalTime.of(0, 0); // fallback
            }
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
        aiStatusLabel.setVisible(false);
        aiStatusLabel.setManaged(false);
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

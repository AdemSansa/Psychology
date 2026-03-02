package Controllers.Question;

import Entities.Question;
import Service.QuestionService;
import Service.QuestionTransformService;
import Service.TranslationService;
import Service.VoiceRecordingService;
import Service.SpeechToTextService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class QuestionAddController implements Initializable {

    @FXML
    private TextArea txtQuestionText;

    @FXML
    private CheckBox chkRequired;

    @FXML
    private TextField txtImagePath;

    @FXML
    private Button btnAdd;

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnSuggest;

    @FXML
    private VBox suggestionsContainer;

    @FXML
    private Label lblSuggestionStatus;

    @FXML
    private Button btnRecord;

    @FXML
    private Label lblRecordStatus;

    private final QuestionService questionService = new QuestionService();
    private final QuestionTransformService transformService = new QuestionTransformService();
    private final TranslationService translationService = new TranslationService();
    private final VoiceRecordingService recordingService = new VoiceRecordingService();
    private final SpeechToTextService sttService = new SpeechToTextService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Default to required
        chkRequired.setSelected(true);
    }

    @FXML
    void handleAdd(ActionEvent event) {
        try {
            String questionText = txtQuestionText.getText();
            int orderIndex = 0; // Defaulting to 0 for now as requested
            boolean required = chkRequired.isSelected();
            String imagePath = txtImagePath.getText();

            if (questionText == null || questionText.trim().isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Question text cannot be empty.");
                return;
            }

            Question q = new Question(imagePath, questionText, orderIndex, required);
            questionService.create(q);

            showAlert(Alert.AlertType.INFORMATION, "Success", "Question added successfully!");
            // Close window or clear fields
            txtQuestionText.clear();
            txtImagePath.clear();
            chkRequired.setSelected(true);
            hideSuggestions();

        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please check your input.");
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to add question: " + e.getMessage());
        }
    }

    @FXML
    void handleCancel(ActionEvent event) {
        // Close the dialog/window
        btnCancel.getScene().getWindow().hide();
    }

    @FXML
    void handleSuggest(ActionEvent event) {
        String questionText = txtQuestionText.getText();
        if (questionText == null || questionText.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Question", "Please type a question first to get suggestions.");
            return;
        }

        // Show loading state
        btnSuggest.setDisable(true);
        btnSuggest.setText("⏳ Loading...");
        showStatus("Fetching AI suggestions...");
        suggestionsContainer.getChildren().clear();

        // Run API call in background thread
        Task<List<String>> task = new Task<>() {
            @Override
            protected List<String> call() throws Exception {
                return transformService.getSuggestions(questionText.trim());
            }
        };

        task.setOnSucceeded(e -> {
            List<String> suggestions = task.getValue();
            Platform.runLater(() -> {
                btnSuggest.setDisable(false);
                btnSuggest.setText("✨ Get AI Suggestions");

                if (suggestions == null || suggestions.isEmpty()) {
                    showStatus("No suggestions returned. Try a different question.");
                } else {
                    showStatus("Double-click a suggestion or use translation buttons:");
                    displaySuggestions(suggestions);
                }
            });
        });

        task.setOnFailed(e -> {
            Platform.runLater(() -> {
                btnSuggest.setDisable(false);
                btnSuggest.setText("✨ Get AI Suggestions");
                Throwable ex = task.getException();

                String errorMsg = "Could not reach the AI service. Make sure the server is running on port 8000.";
                if (ex != null && ex.getMessage() != null) {
                    errorMsg = ex.getMessage();
                }
                showStatus("⚠ Error: " + errorMsg);
            });
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    void handleToggleRecord(ActionEvent event) {
        if (!recordingService.isRecording()) {
            startRecording();
        } else {
            stopAndTranscribe();
        }
    }

    private void startRecording() {
        try {
            recordingService.startRecording();
            btnRecord.setText("⬛"); // Stop icon
            btnRecord.setStyle("-fx-text-fill: #e74c3c;"); // Red color for recording
            lblRecordStatus.setText("🔴 Recording... Speak now");
            lblRecordStatus.setVisible(true);
            showStatus("Microphone is active...");
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Recording Error", "Could not start recording: " + e.getMessage());
        }
    }

    private void stopAndTranscribe() {
        recordingService.stopRecording();
        btnRecord.setText("🎤");
        btnRecord.setDisable(true);
        btnRecord.setStyle(""); // Reset style
        lblRecordStatus.setText("⏳ Processing audio...");

        Task<String> sttTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return sttService.transcribe(recordingService.getRecordedFile());
            }
        };

        sttTask.setOnSucceeded(e -> {
            String text = sttTask.getValue();
            Platform.runLater(() -> {
                btnRecord.setDisable(false);
                lblRecordStatus.setVisible(false);
                if (text != null && !text.trim().isEmpty()) {
                    txtQuestionText.setText(text);
                    showStatus("✓ Voice transcription applied!");
                } else {
                    showStatus("⚠ No speech detected or transcription empty.");
                }
            });
        });

        sttTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                btnRecord.setDisable(false);
                lblRecordStatus.setVisible(false);
                Throwable ex = sttTask.getException();
                String error = (ex != null) ? ex.getMessage() : "Unknown error";
                showStatus("⚠ Transcription failed: " + error);
                if (error.contains("API Key not configured")) {
                    showAlert(Alert.AlertType.WARNING, "Configuration Needed",
                            "Speech-to-Text requires an AssemblyAI API Key. Please add it to SpeechToTextService.java");
                }
            });
        });

        Thread thread = new Thread(sttTask);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Displays suggestion cards in the suggestions container.
     */
    private void displaySuggestions(List<String> suggestions) {
        suggestionsContainer.getChildren().clear();
        suggestionsContainer.setVisible(true);
        suggestionsContainer.setManaged(true);

        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);
            VBox card = createSuggestionCard(i + 1, suggestion);
            suggestionsContainer.getChildren().add(card);
        }
    }

    /**
     * Creates a clickable suggestion card with translation buttons.
     */
    private VBox createSuggestionCard(int index, String text) {
        VBox card = new VBox(10);
        card.getStyleClass().add("suggestion-card");
        card.setPadding(new Insets(12, 15, 12, 15));

        Label textLabel = new Label(index + ". " + text);
        textLabel.setWrapText(true);
        textLabel.setMaxWidth(Double.MAX_VALUE);
        textLabel.setCursor(Cursor.HAND);
        textLabel.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                txtQuestionText.setText(text);
                showStatus("✓ English version applied!");
            }
        });

        HBox translateBox = new HBox(10);
        translateBox.setAlignment(Pos.CENTER_LEFT);

        Button btnEn = new Button("🇬🇧 EN");
        btnEn.getStyleClass().add("btn-lang");
        btnEn.setOnAction(e -> {
            txtQuestionText.setText(text);
            showStatus("✓ English version applied!");
        });

        Button btnFr = new Button("🇫🇷 FR");
        btnFr.getStyleClass().add("btn-lang");
        btnFr.setOnAction(e -> fetchAndApplyTranslation(text, "en", "fr"));

        Button btnAr = new Button("🇸🇦 AR");
        btnAr.getStyleClass().add("btn-lang");
        btnAr.setOnAction(e -> fetchAndApplyTranslation(text, "en", "ar"));

        translateBox.getChildren().addAll(btnEn, btnFr, btnAr);
        card.getChildren().addAll(textLabel, translateBox);

        return card;
    }

    private void fetchAndApplyTranslation(String text, String from, String to) {
        showStatus("Translating...");
        Task<String> translationTask = new Task<>() {
            @Override
            protected String call() throws Exception {
                return translationService.translate(text, from, to);
            }
        };

        translationTask.setOnSucceeded(e -> {
            String translated = translationTask.getValue();
            Platform.runLater(() -> {
                txtQuestionText.setText(translated);
                showStatus("✓ " + (to.equals("fr") ? "French" : "Arabic") + " translation applied!");
            });
        });

        translationTask.setOnFailed(e -> {
            Platform.runLater(() -> {
                showStatus("⚠ Translation failed: " + translationTask.getException().getMessage());
            });
        });

        Thread thread = new Thread(translationTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void showStatus(String message) {
        lblSuggestionStatus.setText(message);
        lblSuggestionStatus.setVisible(true);
        lblSuggestionStatus.setManaged(true);
    }

    private void hideSuggestions() {
        suggestionsContainer.getChildren().clear();
        suggestionsContainer.setVisible(false);
        suggestionsContainer.setManaged(false);
        lblSuggestionStatus.setVisible(false);
        lblSuggestionStatus.setManaged(false);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

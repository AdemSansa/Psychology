package Controllers.Question;

import Entities.Question;
import Service.QuestionService;
import Service.QuestionTransformService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

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

    private final QuestionService questionService = new QuestionService();
    private final QuestionTransformService transformService = new QuestionTransformService();

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
                    showStatus("Click a suggestion to use it:");
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

    /**
     * Displays suggestion cards in the suggestions container.
     */
    private void displaySuggestions(List<String> suggestions) {
        suggestionsContainer.getChildren().clear();
        suggestionsContainer.setVisible(true);
        suggestionsContainer.setManaged(true);

        for (int i = 0; i < suggestions.size(); i++) {
            String suggestion = suggestions.get(i);
            Label card = createSuggestionCard(i + 1, suggestion);
            suggestionsContainer.getChildren().add(card);
        }
    }

    /**
     * Creates a clickable suggestion card.
     */
    private Label createSuggestionCard(int index, String text) {
        Label card = new Label(index + ". " + text);
        card.setWrapText(true);
        card.setMaxWidth(Double.MAX_VALUE);
        card.setPadding(new Insets(12, 15, 12, 15));
        card.getStyleClass().add("suggestion-card");
        card.setCursor(Cursor.HAND);

        // On click, fill the question text field
        card.setOnMouseClicked(e -> {
            txtQuestionText.setText(text);
            showStatus("✓ Suggestion applied!");
        });

        return card;
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

package Controllers.QuizAssesment;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.stage.Stage;

public class QuizResultController {

    @FXML
    private ProgressIndicator resultIndicator;

    @FXML
    private Label percentageLabel;

    @FXML
    private Label moodLabel;

    @FXML
    private Label recommendationLabel;

    @FXML
    private Button closeButton;

    public void setResults(int score, int maxScore) {
        double percentage = (double) score / maxScore;
        resultIndicator.setProgress(percentage);
        percentageLabel.setText(String.format("%.0f%%", percentage * 100));

        analyzeMood(percentage);
    }

    private void analyzeMood(double percentage) {
        String mood;
        String recommendation;
        String color;

        // Analysis Logic:
        // Assuming higher score typically means higher intensity of symptoms (e.g.,
        // Depression/Anxiety scale)
        // Or if it's a "Happiness" quiz, higher is better.
        // For a general "Assessment", let's assume:
        // 0-33%: Low Intensity / Good / Stable
        // 34-66%: Moderate / Neutral / Warning
        // 67-100%: High Intensity / Bad / Urgent

        // TODO: This logic can be inverted or customized based on Quiz Type if needed
        // later.

        if (percentage < 0.34) {
            mood = "Good / Stable";
            recommendation = "You seem to be doing well! Your responses indicate a positive state of mind. Keep engaging in activities that make you happy.";
            color = "#27ae60"; // Green
        } else if (percentage < 0.67) {
            mood = "Moderate / Warning";
            recommendation = "You're showing some signs of distress or varying mood. It might be helpful to take a break, practice mindfulness, or talk to a friend.";
            color = "#f39c12"; // Orange
        } else {
            mood = "High Stress / Attention Needed";
            recommendation = "Your responses indicate high levels of stress or distress. We strongly recommend consulting with a professional or reaching out to a support system.";
            color = "#c0392b"; // Red
        }

        moodLabel.setText(mood);
        moodLabel.setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-font-size: 24px;");
        recommendationLabel.setText(recommendation);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) closeButton.getScene().getWindow();
        stage.close();
    }
}

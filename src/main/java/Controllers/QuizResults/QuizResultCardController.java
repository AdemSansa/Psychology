package Controllers.QuizResults;

import Entities.QuizResult;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;

public class QuizResultCardController {

    @FXML
    private Label categoryLabel;

    @FXML
    private Label dateLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Circle scoreIndicator;

    @FXML
    private Label scoreLabel;

    @FXML
    private Label moodLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    /**
     * Set the quiz result data for this card
     * 
     * @param result The QuizResult to display
     */
    public void setResultData(QuizResult result) {
        if (result == null) {
            return;
        }

        // Set quiz information
        if (result.getQuiz() != null) {
            titleLabel.setText(result.getQuiz().getTitle());
            categoryLabel.setText(result.getQuiz().getCategory());
        }

        // Format and set date
        if (result.getTakenAt() != null) {
            dateLabel.setText(result.getTakenAt().format(DATE_FORMATTER));
        }

        // Calculate and display percentage
        double percentage = (double) result.getResult() / 100.0;
        scoreLabel.setText(String.format("%.0f%%", percentage * 100));

        // Set mood with color coding
        String mood = result.getMood();
        moodLabel.setText(mood != null ? mood : "N/A");

        // Apply color based on mood/percentage
        Color indicatorColor;
        if (percentage < 0.34) {
            indicatorColor = Color.web("#27ae60"); // Green
        } else if (percentage < 0.67) {
            indicatorColor = Color.web("#f39c12"); // Orange
        } else {
            indicatorColor = Color.web("#c0392b"); // Red
        }

        scoreIndicator.setFill(indicatorColor);
        scoreIndicator.setStroke(indicatorColor.darker());
    }
}

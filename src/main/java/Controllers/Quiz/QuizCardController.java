package Controllers.Quiz;

import Entities.Quiz;
import interfaces.QuizListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class QuizCardController {

    @FXML
    private Label categoryLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label questionsLabel;

    @FXML
    private Label statusLabel;

    private Quiz quiz;
    private QuizListener listener;

    public void setData(Quiz quiz, QuizListener listener) {
        this.quiz = quiz;
        this.listener = listener;

        titleLabel.setText(quiz.getTitle());
        categoryLabel.setText(quiz.getCategory());
        questionsLabel.setText(quiz.getTotalQuestions() + " Questions");

        if (quiz.isActive()) {
            statusLabel.setText("Active");
            statusLabel.getStyleClass().removeAll("card-status-inactive");
            statusLabel.getStyleClass().add("card-status-active");
        } else {
            statusLabel.setText("Inactive");
            statusLabel.getStyleClass().removeAll("card-status-active");
            statusLabel.getStyleClass().add("card-status-inactive");
        }
    }

    @FXML
    void handleEdit(ActionEvent event) {
        if (listener != null) {
            listener.onEdit(quiz);
        }
    }

    @FXML
    void handleDelete(ActionEvent event) {
        if (listener != null) {
            listener.onDelete(quiz);
        }
    }
}

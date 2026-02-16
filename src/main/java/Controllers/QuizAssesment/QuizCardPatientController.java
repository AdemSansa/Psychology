package Controllers.QuizAssesment;

import Entities.Quiz;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class QuizCardPatientController {

    @FXML
    private Label categoryLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label questionsLabel;

    private Quiz quiz;

    public void setQuizData(Quiz quiz) {
        this.quiz = quiz;
        titleLabel.setText(quiz.getTitle());
        categoryLabel.setText(quiz.getCategory());
        questionsLabel.setText(quiz.getTotalQuestions() + " Questions");
    }

    @FXML
    private void handleStart() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/psy/QuizAssesment/QuizTaking.fxml"));
            Parent root = loader.load();

            QuizTakingController controller = loader.getController();
            controller.setQuiz(this.quiz);

            Stage stage = new Stage();
            stage.setTitle("Take Quiz: " + quiz.getTitle());
            stage.setScene(new Scene(root));
            stage.show();

            // Optional: Close the current window if needed, or keep it open as a dashboard
            // ((Stage) titleLabel.getScene().getWindow()).close();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error starting quiz: " + e.getMessage());
        }
    }
}

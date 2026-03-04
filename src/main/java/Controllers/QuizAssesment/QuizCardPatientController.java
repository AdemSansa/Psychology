package Controllers.QuizAssesment;

import Entities.Quiz;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import Service.QuizResultService;

public class QuizCardPatientController {

    @FXML
    private Label categoryLabel;

    @FXML
    private Label titleLabel;

    @FXML
    private Label questionsLabel;

    @FXML
    private Label timesTakenLabel;

    private Quiz quiz;
    private final QuizResultService quizResultService = new QuizResultService();

    public void setQuizData(Quiz quiz) {
        this.quiz = quiz;
        titleLabel.setText(quiz.getTitle());
        categoryLabel.setText(quiz.getCategory());
        questionsLabel.setText(quiz.getTotalQuestions() + " Questions");

        try {
            int timesTaken = quizResultService.getTimesTaken(quiz.getId());
            timesTakenLabel.setText("📈 Taken " + timesTaken + " times");
        } catch (java.sql.SQLException e) {
            timesTakenLabel.setText("📈 Taken 0 times");
            e.printStackTrace();
        }
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

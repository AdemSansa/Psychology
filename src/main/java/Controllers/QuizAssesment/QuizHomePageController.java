package Controllers.QuizAssesment;

import Entities.Quiz;
import Service.QuizService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox; // Import VBox just in case, though we expect VBox/AnchorPane root
import util.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class QuizHomePageController implements Initializable {

    @FXML
    private FlowPane quizContainer;

    private QuizService quizService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        quizService = new QuizService();
        loadQuizzes();
    }

    private void loadQuizzes() {
        try {
            List<Quiz> quizzes = quizService.list();
            quizContainer.getChildren().clear();

            for (Quiz quiz : quizzes) {
                if (quiz.isActive()) { // Only show active quizzes to patients
                    try {
                        FXMLLoader loader = new FXMLLoader(
                                getClass().getResource("/com/example/psy/QuizAssesment/QuizCardPatient.fxml"));
                        Parent cardNode = loader.load();

                        QuizCardPatientController controller = loader.getController();
                        controller.setQuizData(quiz);

                        quizContainer.getChildren().add(cardNode);
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("Error loading quiz card: " + e.getMessage());
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error fetching quizzes: " + e.getMessage());
        }
    }

    /**
     * Handle View Past Results button - navigate to results history
     */
    @FXML
    private void handleViewResults() {
        SceneManager.loadPage( "/com/example/psy/QuizResults/QuizResultsHistory.fxml");

    }
}

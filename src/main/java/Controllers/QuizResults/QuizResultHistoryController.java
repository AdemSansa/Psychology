package Controllers.QuizResults;

import Entities.QuizResult;
import Entities.User;
import Service.QuizResultService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.SceneManager;
import util.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class QuizResultHistoryController implements Initializable {

    @FXML
    private FlowPane resultsContainer;

    @FXML
    private VBox emptyStateContainer;

    @FXML
    private Label subtitleLabel;

    private QuizResultService quizResultService;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        quizResultService = new QuizResultService();
        loadUserResults();
    }

    /**
     * Load quiz results for the currently logged-in user
     */
    private void loadUserResults() {
        User currentUser = Session.getInstance().getUser();

        if (currentUser == null) {
            showEmptyState("Please log in to view your quiz history");
            return;
        }

        try {
            List<QuizResult> results = quizResultService.getResultsByUserId(currentUser.getId());

            if (results.isEmpty()) {
                showEmptyState(null);
            } else {
                displayResults(results);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading quiz results: " + e.getMessage());
            showEmptyState("Error loading results. Please try again later.");
        }
    }

    /**
     * Display quiz results as cards
     */
    private void displayResults(List<QuizResult> results) {
        resultsContainer.getChildren().clear();
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);

        for (QuizResult result : results) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/psy/QuizResults/QuizResultCard.fxml"));
                Parent cardNode = loader.load();

                QuizResultCardController controller = loader.getController();
                controller.setResultData(result);

                resultsContainer.getChildren().add(cardNode);
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Error loading result card: " + e.getMessage());
            }
        }
    }

    /**
     * Show empty state with optional custom message
     */
    private void showEmptyState(String customMessage) {
        resultsContainer.getChildren().clear();
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);

        if (customMessage != null) {
            subtitleLabel.setText(customMessage);
        }
    }

    /**
     * Handle back button - navigate to quiz home page
     */
    @FXML
    private void handleBack() {
        SceneManager.loadPage( "/com/example/psy/QuizAssesment/quizList.fxml");

    }
}

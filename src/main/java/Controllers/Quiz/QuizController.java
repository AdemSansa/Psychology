package Controllers.Quiz;

import Entities.Quiz;
import Service.QuizService;
import interfaces.QuizListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuizController implements Initializable, QuizListener {

    @FXML
    private GridPane quizGrid;

    private final QuizService quizService = new QuizService();
    private List<Quiz> quizList = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadData();
    }

    private void loadData() {
        try {
            quizList = quizService.list();
            populateGrid();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to load quizzes: " + e.getMessage());
        }
    }

    private void populateGrid() {
        quizGrid.getChildren().clear();
        int column = 0;
        int row = 1;

        try {
            for (Quiz quiz : quizList) {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/example/psy/Quiz/QuizCard.fxml"));
                VBox card = fxmlLoader.load();

                QuizCardController cardController = fxmlLoader.getController();
                cardController.setData(quiz, this);

                if (column == 4) { // 4 cards per row
                    column = 0;
                    row++;
                }

                quizGrid.add(card, column++, row);
                // Set grid margins
                GridPane.setMargin(card, new Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "UI Error", "Failed to load quiz cards: " + e.getMessage());
        }
    }

    @Override
    public void onEdit(Quiz quiz) {
        handleEditQuiz(quiz);
    }

    @Override
    public void onDelete(Quiz quiz) {
        handleDeleteQuiz(quiz);
    }

    @FXML
    void handleAddQuiz(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/psy/Quiz/QuizAdd.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Add New Quiz");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Add Quiz window: " + e.getMessage());
        }
    }

    private void handleEditQuiz(Quiz quiz) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/psy/Quiz/QuizEdit.fxml"));
            Parent root = loader.load();

            QuizEditController controller = loader.getController();
            controller.setQuiz(quiz);

            Stage stage = new Stage();
            stage.setTitle("Edit Quiz");
            stage.setScene(new Scene(root));
            stage.showAndWait();
            loadData();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to open Edit Quiz window: " + e.getMessage());
        }
    }

    private void handleDeleteQuiz(Quiz quiz) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Quiz");
        alert.setHeaderText("Are you sure you want to delete quiz: " + quiz.getTitle() + "?");
        alert.setContentText("This action cannot be undone.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                if (quiz.getId() != null) {
                    quizService.delete(quiz.getId().intValue());
                    loadData(); // Refresh grid
                }
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to delete quiz: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    public void goBack() {
        SceneManager.switchScene( "/com/example/psy/intro/Home.fxml");
    }
}

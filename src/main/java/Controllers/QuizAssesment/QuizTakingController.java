package Controllers.QuizAssesment;

import Entities.Question;
import Entities.Quiz;
import Entities.QuizResult;
import Entities.User; // Assuming User entity exists and we can get current user
import Service.QuizResultService;
import Service.QuizService; // To fetch questions
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class QuizTakingController {

    @FXML
    private Label progressLabel;

    @FXML
    private ProgressBar quizProgressBar;

    @FXML
    private ImageView questionImage;

    @FXML
    private Label questionTextLabel;

    @FXML
    private ToggleGroup optionsGroup;

    @FXML
    private RadioButton opt0;
    @FXML
    private RadioButton opt1;
    @FXML
    private RadioButton opt2;
    @FXML
    private RadioButton opt3;
    @FXML
    private RadioButton opt4;
    @FXML
    private RadioButton opt5;

    @FXML
    private Button nextButton;

    private Quiz quiz;
    private List<Question> questions = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int totalScore = 0;

    private QuizService quizService;
    private QuizResultService quizResultService;

    // This method is called from the previous controller to set the quiz
    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
        this.quizService = new QuizService();
        this.quizResultService = new QuizResultService();
        loadQuestions();
    }

    private void loadQuestions() {
        try {
            this.questions = quizService.getQuestionsForQuiz(quiz.getId().intValue());
            if (questions == null || questions.isEmpty()) {
                showAlert("Error", "No questions found for this quiz.");
                closeWindow();
                return;
            }
            currentQuestionIndex = 0;
            totalScore = 0;
            showQuestion();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Database Error", "Failed to load questions: " + e.getMessage());
        }
    }

    private void showQuestion() {
        if (currentQuestionIndex < questions.size()) {
            Question q = questions.get(currentQuestionIndex);

            // Update UI
            questionTextLabel.setText(q.getQuestionText());

            // Image handling (if applicable)
            if (q.getImagePath() != null && !q.getImagePath().isEmpty()) {
                try {
                    // Check if it's a file path or URL
                    // Implementation depends on how image paths are stored
                    Image img = new Image(q.getImagePath()); // Or file input stream
                    questionImage.setImage(img);
                    questionImage.setVisible(true);
                    questionImage.setManaged(true);
                } catch (Exception e) {
                    // Fallback if image fails to load
                    questionImage.setVisible(false);
                    questionImage.setManaged(false);
                }
            } else {
                questionImage.setVisible(false);
                questionImage.setManaged(false);
            }

            // Update Progress
            int questionNum = currentQuestionIndex + 1;
            progressLabel.setText("Question " + questionNum + " of " + questions.size());
            quizProgressBar.setProgress((double) questionNum / questions.size());

            // Reset Selection
            if (optionsGroup.getSelectedToggle() != null) {
                optionsGroup.getSelectedToggle().setSelected(false);
            }

            // Update Button Text
            if (currentQuestionIndex == questions.size() - 1) {
                nextButton.setText("Submit Quiz");
            } else {
                nextButton.setText("Next Question");
            }
        }
    }

    @FXML
    private void handleNext() {
        // Get selected value
        RadioButton selected = (RadioButton) optionsGroup.getSelectedToggle();
        if (selected == null) {
            showAlert("Selection Required", "Please select an option to continue.");
            return;
        }

        int score = getScoreFromSelection(selected);
        totalScore += score;

        currentQuestionIndex++;
        if (currentQuestionIndex < questions.size()) {
            showQuestion();
        } else {
            finishQuiz();
        }
    }

    private int getScoreFromSelection(RadioButton selected) {
        if (selected == opt0)
            return 0;
        if (selected == opt1)
            return 1;
        if (selected == opt2)
            return 2;
        if (selected == opt3)
            return 3;
        if (selected == opt4)
            return 4;
        if (selected == opt5)
            return 5;
        return 0;
    }

    private void finishQuiz() {
        try {
            int maxScore = questions.size() * 5;
            double percentage = (double) totalScore / maxScore;

            QuizResult result = new QuizResult();
            result.setQuiz(quiz);
            result.setScore(totalScore);
            result.setResult((int) (percentage * 100)); // Store as percentage 0-100
            result.setTakenAt(LocalDateTime.now());

            // Basic Mood Determination for DB
            String mood;
            if (percentage < 0.34) {
                mood = "Good";
            } else if (percentage < 0.67) {
                mood = "Moderate";
            } else {
                mood = "Bad/Stress";
            }
            result.setMood(mood);

            // Mock User for now - In real app, get from Session/AuthService
            User user = new User();
            user.setId(6); // Placeholder ID
            result.setUser(user);

            quizResultService.create(result);

            // Open Result View
            openResultView(totalScore, maxScore);

            // Close current quiz window
            closeWindow();

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to save results: " + e.getMessage());
        }
    }

    private void openResultView(int score, int maxScore) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/psy/QuizAssesment/QuizResultView.fxml"));
            Parent root = loader.load();

            QuizResultController controller = loader.getController();
            controller.setResults(score, maxScore);

            Stage stage = new Stage();
            stage.setTitle("Assessment Results");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("UI Error", "Failed to load result view: " + e.getMessage());
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) nextButton.getScene().getWindow();
        stage.close();
    }
}

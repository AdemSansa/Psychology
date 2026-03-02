package Controllers.dashboards;

import Service.QuizResultService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.net.URL;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class QuizDashboardController implements Initializable {

    @FXML
    private Label totalQuizzesLabel;

    @FXML
    private Label uniqueUsersLabel;

    @FXML
    private Label averageScoreLabel;

    @FXML
    private Label mostPopularQuizLabel;

    @FXML
    private PieChart moodPieChart;

    @FXML
    private BarChart<String, Number> quizPopularityChart;

    private final QuizResultService quizResultService = new QuizResultService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadStatistics();
        loadCharts();
    }

    private void loadStatistics() {
        try {
            int totalQuizzes = quizResultService.getTotalQuizzesTaken();
            int uniqueUsers = quizResultService.getUniqueUsersCount();
            double averageScore = quizResultService.getAverageScore();
            String mostPopularQuiz = quizResultService.getMostPopularQuizName();

            totalQuizzesLabel.setText(String.valueOf(totalQuizzes));
            uniqueUsersLabel.setText(String.valueOf(uniqueUsers));
            averageScoreLabel.setText(String.format("%.1f", averageScore));
            mostPopularQuizLabel.setText(mostPopularQuiz);
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading quiz statistics.");
        }
    }

    private void loadCharts() {
        try {
            // Load Mood Distribution
            Map<String, Integer> moodStats = quizResultService.getMoodDistribution();
            for (Map.Entry<String, Integer> entry : moodStats.entrySet()) {
                PieChart.Data slice = new PieChart.Data(entry.getKey() + " (" + entry.getValue() + ")",
                        entry.getValue());
                moodPieChart.getData().add(slice);
            }

            // Load Quiz Popularity
            Map<String, Integer> popularityStats = quizResultService.getQuizPopularity();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName("Takes");
            for (Map.Entry<String, Integer> entry : popularityStats.entrySet()) {
                series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
            quizPopularityChart.getData().add(series);

        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("Error loading chart data.");
        }
    }
}

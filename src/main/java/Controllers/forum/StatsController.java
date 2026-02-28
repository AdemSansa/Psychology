package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import Service.SentimentAnalysisService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatsController {

    @FXML
    private PieChart pieResponded;

    @FXML
    private PieChart pieSentiment;

    @FXML
    private BarChart barDailyActivity;

    @FXML
    private Label lblTotalReviews;

    @FXML
    private Label lblTotalReplies;

    @FXML
    private Label lblActiveUsers;

    @FXML
    private Label lblSatisfactionRate;

    @FXML
    private Label lblBusiestDay;

    @FXML
    private Button btnBackToMain;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();
    private final SentimentAnalysisService sentimentService = new SentimentAnalysisService();

    @FXML
    public void initialize() {
        loadStats();

        btnBackToMain.setOnAction(e -> {
            Stage stage = (Stage) btnBackToMain.getScene().getWindow();
            stage.close();
        });
    }

    private void loadStats() {
        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            // Basic statistics
            long totalReviews = reviews.size();
            long totalReplies = replies.size();
            long responded = reviews.stream()
                    .filter(r -> replies.stream().anyMatch(rep -> rep.getReviewId() == r.getIdReview()))
                    .count();
            long notResponded = totalReviews - responded;

            // Sentiment analysis statistics
            Map<String, Long> sentimentStats = reviews.stream()
                    .collect(Collectors.groupingBy(
                            review -> sentimentService.analyzeSentiment(review.getContent()),
                            Collectors.counting()
                    ));

            // Daily activity analysis
            Map<String, Long> dailyActivity = reviews.stream()
                    .collect(Collectors.groupingBy(
                            review -> review.getCreatedAt().toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                            Collectors.counting()
                    ));

            // Calculate percentages
            double responseRate = totalReviews > 0 ? (responded * 100.0 / totalReviews) : 0;
            double satisfactionRate = responded > 0 ? 
                    (sentimentStats.getOrDefault("😊 Positif", 0L) * 100.0 / responded) : 0;
            String busiestDay = dailyActivity.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("N/A");

            // Update pie charts with percentages
            ObservableList<PieChart.Data> dataResponded = FXCollections.observableArrayList(
                    new PieChart.Data("Répondus (" + String.format("%.1f", responseRate) + "%)", responded),
                    new PieChart.Data("Non répondus (" + String.format("%.1f", 100 - responseRate) + "%)", notResponded)
            );
            pieResponded.setData(dataResponded);

            ObservableList<PieChart.Data> dataSentiment = FXCollections.observableArrayList();
            sentimentStats.forEach((sentiment, count) -> {
                double percentage = totalReviews > 0 ? (count * 100.0 / totalReviews) : 0;
                String label = sentiment + " (" + String.format("%.1f", percentage) + "%)";
                dataSentiment.add(new PieChart.Data(label, count));
            });
            pieSentiment.setData(dataSentiment);

            // Update bar chart - Daily Activity
            XYChart.Series<String, Number> dailySeries = new XYChart.Series<>();
            dailySeries.setName("Activité Quotidienne");
            dailyActivity.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .limit(7) // Last 7 days
                    .forEach(entry -> dailySeries.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue())));
            barDailyActivity.getData().clear();
            barDailyActivity.getData().add(dailySeries);

            // Update labels with percentages
            lblTotalReviews.setText("📝 " + totalReviews + " avis");
            lblTotalReplies.setText("💬 " + totalReplies + " réponses");
            lblActiveUsers.setText("👥 " + String.format("%.1f", responseRate) + "%");
            lblSatisfactionRate.setText("😊 " + String.format("%.1f", satisfactionRate) + "%");
            lblBusiestDay.setText("📅 " + busiestDay);

            // Apply colors
            applyChartColors();

        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyChartColors() {
        // Response rate colors
        pieResponded.getData().forEach(d -> {
            if (d.getName().contains("Répondus")) {
                d.getNode().setStyle("-fx-pie-color: #4CAF50;");
            } else if (d.getName().contains("Non répondus")) {
                d.getNode().setStyle("-fx-pie-color: #EF5350;");
            }
        });

        // Sentiment colors
        pieSentiment.getData().forEach(d -> {
            if (d.getName().contains("Positif")) {
                d.getNode().setStyle("-fx-pie-color: #4CAF50;");
            } else if (d.getName().contains("Négatif")) {
                d.getNode().setStyle("-fx-pie-color: #EF5350;");
            } else if (d.getName().contains("Neutre")) {
                d.getNode().setStyle("-fx-pie-color: #2196F3;");
            } else {
                d.getNode().setStyle("-fx-pie-color: #9E9E9E;");
            }
        });
    }
}
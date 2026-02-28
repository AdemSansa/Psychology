package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.Duration;
import java.util.List;

public class StatsController {

    @FXML
    private PieChart pieResponded;

    @FXML
    private PieChart pieFast;

    @FXML
    private Button btnBack;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();

    @FXML
    public void initialize() {
        loadStats();

        btnBack.setOnAction(e -> {
            Stage stage = (Stage) btnBack.getScene().getWindow();
            stage.close();
        });
    }

    private void loadStats() {
        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            long totalReviews = reviews.size();
            long responded = reviews.stream()
                    .filter(r -> replies.stream().anyMatch(rep -> rep.getReviewId() == r.getIdReview()))
                    .count();
            long notResponded = totalReviews - responded;

            ObservableList<PieChart.Data> dataResponded = FXCollections.observableArrayList(
                    new PieChart.Data("Répondus", responded),
                    new PieChart.Data("Non répondus", notResponded)
            );
            pieResponded.setData(dataResponded);

            long fastReplies = replies.stream()
                    .filter(rep -> {
                        Review rev = reviews.stream()
                                .filter(r -> r.getIdReview() == rep.getReviewId())
                                .findFirst().orElse(null);
                        if (rev != null) {
                            Duration diff = Duration.between(rev.getCreatedAt(), rep.getCreatedAt());
                            return diff.toMinutes() <= 60;
                        }
                        return false;
                    })
                    .count();
            long slowReplies = replies.size() - fastReplies;

            ObservableList<PieChart.Data> dataFast = FXCollections.observableArrayList(
                    new PieChart.Data("Réponses < 1h", fastReplies),
                    new PieChart.Data("Autres", slowReplies)
            );
            pieFast.setData(dataFast);

            // Couleurs
            pieResponded.getData().forEach(d -> {
                if (d.getName().equals("Répondus")) d.getNode().setStyle("-fx-pie-color: #4CAF50;");
                else d.getNode().setStyle("-fx-pie-color: #EF5350;");
            });

            pieFast.getData().forEach(d -> {
                if (d.getName().equals("Réponses < 1h")) d.getNode().setStyle("-fx-pie-color: #FF9800;");
                else d.getNode().setStyle("-fx-pie-color: #9E9E9E;");
            });

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
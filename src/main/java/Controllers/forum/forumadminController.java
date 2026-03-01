package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import Service.TranslationApiService;
import Service.SentimentAnalysisService;
import util.SceneManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class forumadminController {

    @FXML
    private VBox reviewContainer;

    @FXML
    private Button btnStats;

    @FXML
    private Button btnRefresh;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();
    private final TranslationApiService translationApiService = new TranslationApiService();
    private final SentimentAnalysisService sentimentService = new SentimentAnalysisService();

    @FXML
    public void initialize() {
        loadReviews();

        btnStats.setOnAction(e -> {
            try {
                System.out.println("Loading statistics view...");
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/psy/forum/StatsView.fxml"));
                Stage statsStage = new Stage();
                statsStage.setTitle("📊 Statistiques Forum");
                statsStage.setScene(new Scene(loader.load()));
                statsStage.show();
                
            } catch (Exception ex) {
                System.err.println("Error loading stats view: " + ex.getMessage());
                ex.printStackTrace();
                showAlert("Error", "Failed to load statistics view: " + ex.getMessage());
            }
        });

        btnRefresh.setOnAction(e -> loadReviews());
    }

    private void loadReviews() {
        reviewContainer.getChildren().clear();
        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            reviews.sort(Comparator.comparing(Review::getCreatedAt).reversed());

            for (Review review : reviews) {
                VBox card = createReviewCard(review, replies);
                reviewContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createReviewCard(Review review, List<ReviewReply> replies) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color:#FAF3E0; -fx-padding:15; -fx-background-radius:15; " +
                        "-fx-border-radius:15; -fx-effect:dropshadow(three-pass-box, rgba(0,0,0,0.1),10,0,0,5);");

        Label reviewDate = new Label("🗓 " + review.getCreatedAt());
        reviewDate.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

        Label reviewContent = new Label("💬 " + review.getContent());
        reviewContent.setWrapText(true);
        reviewContent.setStyle("-fx-font-size:14px; -fx-text-fill:#4E342E;");

        // Add sentiment analysis emoji under the content
        String sentiment = sentimentService.analyzeSentiment(review.getContent());
        Label sentimentLabel = new Label(sentiment);
        sentimentLabel.setStyle("-fx-font-weight:bold; -fx-font-size:12px; -fx-text-fill:#666;");

        HBox translateBox = new HBox(10);
        Button btnTranslateEn = new Button("🇬🇧 English");
        Button btnTranslateAr = new Button("🇸🇦 Arabic");

        btnTranslateEn.setOnAction(e -> reviewContent.setText(
                translationApiService.translate(review.getContent(), "en")
        ));
        btnTranslateAr.setOnAction(e -> reviewContent.setText(
                translationApiService.translate(review.getContent(), "ar")
        ));
        translateBox.getChildren().addAll(btnTranslateEn, btnTranslateAr);

        card.getChildren().addAll(reviewDate, reviewContent, sentimentLabel, translateBox);

        replies.stream()
                .filter(r -> r.getReviewId() == review.getIdReview())
                .sorted(Comparator.comparing(ReviewReply::getCreatedAt))
                .forEach(r -> {
                    VBox replyBox = createReplyBox(r);
                    card.getChildren().add(replyBox);
                });

        return card;
    }

    private VBox createReplyBox(ReviewReply r) {
        VBox replyBox = new VBox(5);
        replyBox.setStyle("-fx-background-color:#FFF8E1; -fx-padding:10; -fx-background-radius:12;");

        Label replyDate = new Label("🗓 " + r.getCreatedAt());
        replyDate.setStyle("-fx-font-size:10px; -fx-text-fill:gray;");

        Label replyContent = new Label("👩‍⚕️ Therapist: " + r.getContent());
        replyContent.setWrapText(true);

        // Add sentiment analysis for therapist replies too
        String sentiment = sentimentService.analyzeSentiment(r.getContent());
        Label sentimentLabel = new Label(sentiment);
        sentimentLabel.setStyle("-fx-font-weight:bold; -fx-font-size:11px; -fx-text-fill:#666;");

        HBox translateReplyBox = new HBox(10);
        Button btnTranslateEnReply = new Button("🇬🇧 English");
        Button btnTranslateArReply = new Button("🇸🇦 Arabic");

        btnTranslateEnReply.setOnAction(e -> replyContent.setText(
                translationApiService.translate(r.getContent(), "en")
        ));
        btnTranslateArReply.setOnAction(e -> replyContent.setText(
                translationApiService.translate(r.getContent(), "ar")
        ));
        translateReplyBox.getChildren().addAll(btnTranslateEnReply, btnTranslateArReply);

        replyBox.getChildren().addAll(replyDate, replyContent, sentimentLabel, translateReplyBox);

        return replyBox;
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
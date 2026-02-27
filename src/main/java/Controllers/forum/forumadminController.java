package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import Service.TranslationApiService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

public class forumadminController implements Initializable {

    @FXML
    private VBox reviewContainer;

    @FXML
    private Label statsLabel;

    @FXML
    private Button btnRefresh;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();
    private final TranslationApiService translationService = new TranslationApiService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Action bouton refresh
        btnRefresh.setOnAction(e -> loadReviews());

        loadReviews();
    }

    private void loadReviews() {

        reviewContainer.getChildren().clear();

        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            // Trier les avis par date DESC (le plus récent en haut)
            reviews.sort(Comparator.comparing(Review::getCreatedAt).reversed());

            // Statistiques
            updateStatistics(reviews, replies);

            for (Review review : reviews) {
                VBox reviewCard = createReviewCard(review, replies);
                reviewContainer.getChildren().add(reviewCard);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private VBox createReviewCard(Review review, List<ReviewReply> replies) {

        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color:#FFFFFF;" +
                        "-fx-padding:15;" +
                        "-fx-background-radius:15;" +
                        "-fx-border-radius:15;" +
                        "-fx-effect:dropshadow(three-pass-box, rgba(0,0,0,0.1),10,0,0,5);"
        );

        Label reviewDate = new Label("🗓 " + review.getCreatedAt());
        reviewDate.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

        Label reviewContent = new Label("💬 " + review.getContent());
        reviewContent.setWrapText(true);
        reviewContent.setStyle("-fx-font-size:14px; -fx-text-fill:#4E342E;");

        // Boutons traduction pour l'avis
        HBox translateBox = new HBox(10);
        Button btnEn = new Button("🇬🇧 EN");
        Button btnAr = new Button("🇸🇦 AR");
        styleTranslateButton(btnEn);
        styleTranslateButton(btnAr);

        btnEn.setOnAction(e -> reviewContent.setText(translationService.translate(review.getContent(), "en")));
        btnAr.setOnAction(e -> reviewContent.setText(translationService.translate(review.getContent(), "ar")));

        translateBox.getChildren().addAll(btnEn, btnAr);

        card.getChildren().addAll(reviewDate, reviewContent, translateBox);

        // Ajouter les réponses sous l'avis
        replies.stream()
                .filter(r -> r.getReviewId() == review.getIdReview())
                .sorted(Comparator.comparing(ReviewReply::getCreatedAt)) // réponses du plus ancien au plus récent
                .forEach(reply -> {
                    VBox replyBox = createReplyBox(reply);
                    card.getChildren().add(replyBox);
                });

        return card;
    }

    private VBox createReplyBox(ReviewReply reply) {

        VBox replyBox = new VBox(5);
        replyBox.setStyle(
                "-fx-background-color:#FFF8E1;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:12;"
        );

        Label replyDate = new Label("🗓 " + reply.getCreatedAt());
        replyDate.setStyle("-fx-font-size:10px; -fx-text-fill:gray;");

        Label replyContent = new Label("👩‍⚕️ Therapist: " + reply.getContent());
        replyContent.setWrapText(true);

        // Boutons traduction pour la réponse
        HBox translateBox = new HBox(10);
        Button btnEn = new Button("🇬🇧 EN");
        Button btnAr = new Button("🇸🇦 AR");
        styleTranslateButton(btnEn);
        styleTranslateButton(btnAr);

        btnEn.setOnAction(e -> replyContent.setText(translationService.translate(reply.getContent(), "en")));
        btnAr.setOnAction(e -> replyContent.setText(translationService.translate(reply.getContent(), "ar")));

        translateBox.getChildren().addAll(btnEn, btnAr);

        replyBox.getChildren().addAll(replyDate, replyContent, translateBox);

        return replyBox;
    }

    private void styleTranslateButton(Button btn) {
        btn.setStyle("-fx-background-color:#90CAF9; -fx-text-fill:#0D47A1; " +
                "-fx-background-radius:20; -fx-font-weight:bold; -fx-padding:2 10;");
    }

    private void updateStatistics(List<Review> reviews, List<ReviewReply> replies) {

        long total = reviews.size();
        long responded = replies.stream().map(ReviewReply::getReviewId).distinct().count();

        // Pourcentage avis répondus
        double pctResponded = total == 0 ? 0 : ((double) responded / total) * 100;

        // Pourcentage réponses en moins d'une heure
        long fastReplies = replies.stream()
                .filter(r -> {
                    Review review = reviews.stream()
                            .filter(rv -> rv.getIdReview() == r.getReviewId())
                            .findFirst().orElse(null);
                    if (review == null) return false;
                    Duration duration = Duration.between(review.getCreatedAt(), r.getCreatedAt());
                    return duration.toMinutes() <= 60;
                }).count();

        double pctFast = total == 0 ? 0 : ((double) fastReplies / total) * 100;

        statsLabel.setText(String.format("📊 Total avis: %d | Répondus: %.0f%% | Réponse < 1h: %.0f%%",
                total, pctResponded, pctFast));
    }
}
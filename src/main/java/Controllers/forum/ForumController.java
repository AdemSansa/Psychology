package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import Service.BadWordsApiService;
import Service.TranslationApiService;
import Service.SentimentAnalysisService;
import util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ForumController implements Initializable {

    @FXML
    private VBox reviewContainer;

    @FXML
    private TextArea contentField;

    @FXML
    private Button btnTranslateEn;

    @FXML
    private Button btnTranslateAr;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();

    private final BadWordsApiService badWordsApiService = new BadWordsApiService();
    private final TranslationApiService translationApiService = new TranslationApiService();
    private final SentimentAnalysisService sentimentService = new SentimentAnalysisService();

    private int currentUserId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        reviewContainer.setStyle("-fx-background-color:#F5F5DC;");
        reviewContainer.setSpacing(15);
        reviewContainer.setPadding(new Insets(20));

        if (Session.getInstance().getUser() != null) {
            currentUserId = Session.getInstance().getUser().getId();
        }

        btnTranslateEn.setOnAction(e -> translateToEnglish(e));
        btnTranslateAr.setOnAction(e -> translateToArabic(e));

        loadReviews();
    }

    @FXML
    private void translateToEnglish(ActionEvent event) {
        translateContent("en");
    }

    @FXML
    private void translateToArabic(ActionEvent event) {
        translateContent("ar");
    }

    private void translateContent(String lang) {
        String text = contentField.getText().trim();

        if (text.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Text", "Nothing to translate!");
            return;
        }

        if (badWordsApiService.containsBadWords(text)) {
            showAlert(Alert.AlertType.ERROR, "Forbidden Content",
                    "Cannot translate inappropriate content ❌");
            return;
        }

        String translated = translationApiService.translate(text, lang);

        if (!translated.startsWith("Translation failed")) {
            contentField.setText(translated);
        } else {
            showAlert(Alert.AlertType.ERROR, "Translation Failed", translated);
        }
    }

    @FXML
    private void addReview(ActionEvent event) {

        String content = contentField.getText().trim();

        if (content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Review cannot be empty!");
            return;
        }

        if (content.length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Review must contain at least 10 characters!");
            return;
        }

        if (badWordsApiService.containsBadWords(content)) {
            showAlert(Alert.AlertType.ERROR, "Forbidden Content",
                    "Your review contains inappropriate words ❌");
            return;
        }

        try {
            if (reviewService.isExist(content)) {
                showAlert(Alert.AlertType.WARNING, "Validation Error", "Review already exists!");
                return;
            } else {
                Review review = new Review();
                review.setContent(content);
                review.setIdUser(currentUserId);

                reviewService.create(review);

                contentField.clear();
                loadReviews();

                showAlert(Alert.AlertType.INFORMATION, "Success", "Review added successfully!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add review.");
        }
    }

    private void loadReviews() {

        reviewContainer.getChildren().clear();

        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

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
        card.setStyle(
                "-fx-background-color:#FAF3E0;" +
                        "-fx-padding:15;" +
                        "-fx-background-radius:15;"
        );

        Label date = new Label(review.getCreatedAt().toString());
        date.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

        Label content = new Label(review.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size:14px; -fx-text-fill:#4E342E;");

        // Add sentiment analysis emoji under the content
        String sentiment = sentimentService.analyzeSentiment(review.getContent());
        Label sentimentLabel = new Label(sentiment);
        sentimentLabel.setStyle("-fx-font-weight:bold; -fx-font-size:12px; -fx-text-fill:#666;");

        // 🔹 Boutons traduction Review
        Button enBtn = new Button("🌍 EN");
        Button arBtn = new Button("🌍 AR");

        enBtn.setOnAction(e -> translateLabel(content, "en"));
        arBtn.setOnAction(e -> translateLabel(content, "ar"));

        HBox translateBox = new HBox(5, enBtn, arBtn);

        card.getChildren().addAll(date, content, sentimentLabel, translateBox);

        if (review.getIdUser() == currentUserId) {

            HBox buttonBox = new HBox(10);

            Button editButton = new Button("✏ Edit");
            Button deleteButton = new Button("🗑 Delete");

            editButton.setOnAction(e -> editReview(review));
            deleteButton.setOnAction(e -> deleteReview(review));

            buttonBox.getChildren().addAll(editButton, deleteButton);
            card.getChildren().add(buttonBox);
        }

        for (ReviewReply r : replies) {
            if (r.getReviewId().equals(review.getIdReview())) {
                VBox replyBox = createReplyBox(r);
                card.getChildren().add(replyBox);
            }
        }

        return card;
    }

    private VBox createReplyBox(ReviewReply r) {

        VBox replyBox = new VBox(5);
        replyBox.setStyle(
                "-fx-background-color:#FFF8E1;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:12;"
        );

        Label replyContent = new Label("👩‍⚕️ Therapist: " + r.getContent());
        replyContent.setWrapText(true);
        replyContent.setStyle("-fx-text-fill:#4E342E;");

        Label replyDate = new Label(r.getCreatedAt().toString());
        replyDate.setStyle("-fx-font-size:10px; -fx-text-fill:gray;");

        // Add sentiment analysis for therapist replies too
        String sentiment = sentimentService.analyzeSentiment(r.getContent());
        Label sentimentLabel = new Label(sentiment);
        sentimentLabel.setStyle("-fx-font-weight:bold; -fx-font-size:11px; -fx-text-fill:#666;");

        // 🔹 Boutons traduction Reply
        Button enBtn = new Button("🌍 EN");
        Button arBtn = new Button("🌍 AR");

        enBtn.setOnAction(e -> translateLabel(replyContent, "en"));
        arBtn.setOnAction(e -> translateLabel(replyContent, "ar"));

        HBox translateBox = new HBox(5, enBtn, arBtn);

        replyBox.getChildren().addAll(replyContent, replyDate, sentimentLabel, translateBox);

        return replyBox;
    }

    // ✅ Méthode ajoutée (sans toucher aux autres)
    private void translateLabel(Label label, String lang) {

        String text = label.getText();

        if (badWordsApiService.containsBadWords(text)) return;

        String translated = translationApiService.translate(text, lang);

        if (!translated.startsWith("Translation failed")) {
            label.setText(translated);
        }
    }



    private void editReview(Review review) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Edit Review");
        confirm.setHeaderText("Do you really want to edit this review?");

        confirm.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                TextInputDialog dialog = new TextInputDialog(review.getContent());
                dialog.setTitle("Edit Review");
                dialog.setHeaderText("Edit your review");

                dialog.showAndWait().ifPresent(newContent -> {

                    String updated = newContent.trim();

                    if (updated.isEmpty() || updated.length() < 10) {
                        showAlert(Alert.AlertType.WARNING, "Validation Error",
                                "Review must contain at least 10 characters!");
                        return;
                    }

                    try {
                        review.setContent(updated);
                        reviewService.update(review);
                        loadReviews();
                        showAlert(Alert.AlertType.INFORMATION, "Success",
                                "Review edited successfully!");
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                });
            }
        });
    }

    private void deleteReview(Review review) {

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Review");
        confirm.setHeaderText("Do you really want to delete this review?");

        confirm.showAndWait().ifPresent(response -> {

            if (response == ButtonType.OK) {

                try {
                    reviewService.delete(review.getIdReview());
                    loadReviews();
                    showAlert(Alert.AlertType.INFORMATION,
                            "Success", "Review deleted successfully!");
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String message) {

        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
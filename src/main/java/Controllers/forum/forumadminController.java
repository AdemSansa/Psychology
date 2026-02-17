package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class forumadminController implements Initializable {

    @FXML
    private VBox reviewContainer;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        reviewContainer.setStyle("-fx-background-color:#F5F5DC;");
        reviewContainer.setSpacing(20);
        reviewContainer.setPadding(new Insets(20));

        loadReviews();
    }

    private void loadReviews() {

        reviewContainer.getChildren().clear();

        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            for (Review review : reviews) {

                VBox card = new VBox(10);
                card.setStyle(
                        "-fx-background-color:#FAF3E0;" +
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

                card.getChildren().addAll(reviewDate, reviewContent);

                // Replies
                for (ReviewReply reply : replies) {

                    if (reply.getReviewId() == review.getIdReview()) {

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

                        replyBox.getChildren().addAll(replyDate, replyContent);

                        card.getChildren().add(replyBox);
                    }
                }

                reviewContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

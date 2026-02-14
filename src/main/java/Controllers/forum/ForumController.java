package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class ForumController implements Initializable {

    @FXML
    private VBox reviewContainer;

    @FXML
    private TextArea contentField;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();

    private final int currentUserId = 1;
    private final int therapistId = 2;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadReviews();
    }

    // ADD REVIEW
    @FXML
    private void addReview() {
        try {
            if (contentField.getText().isEmpty()) return;

            Review review = new Review(contentField.getText(), currentUserId);
            reviewService.create(review);

            contentField.clear();
            loadReviews();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // LOAD REVIEWS
    private void loadReviews() {
        reviewContainer.getChildren().clear();

        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            for (Review review : reviews) {

                VBox card = new VBox(10);
                card.setStyle("-fx-background-color: white;" +
                        "-fx-padding: 15;" +
                        "-fx-background-radius: 12;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08),6,0,0,3);");

                Label date = new Label(review.getCreatedAt().toString());
                date.setStyle("-fx-text-fill: gray; -fx-font-size: 11px;");

                Label content = new Label(review.getContent());
                content.setWrapText(true);
                content.setStyle("-fx-font-size: 14px;");

                HBox buttons = new HBox(10);

                Button editBtn = new Button("Edit");
                Button deleteBtn = new Button("Delete");
                Button replyBtn = new Button("Reply");

                // DELETE
                deleteBtn.setOnAction(e -> {
                    try {
                        reviewService.delete(review.getIdReview());
                        loadReviews();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                // EDIT
                editBtn.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog(review.getContent());
                    dialog.setHeaderText("Edit your review");
                    dialog.showAndWait().ifPresent(newText -> {
                        try {
                            review.setContent(newText);
                            reviewService.update(review);
                            loadReviews();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });
                });

                // REPLY
                replyBtn.setOnAction(e -> {
                    TextInputDialog dialog = new TextInputDialog();
                    dialog.setHeaderText("Write a reply");
                    dialog.showAndWait().ifPresent(text -> {
                        try {
                            ReviewReply reply =
                                    new ReviewReply(text,
                                            review.getIdReview(),
                                            therapistId);
                            replyService.create(reply);
                            loadReviews();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                        }
                    });
                });

                buttons.getChildren().addAll(replyBtn, editBtn, deleteBtn);

                card.getChildren().addAll(date, content, buttons);

                // SHOW REPLIES UNDER REVIEW
                for (ReviewReply r : replies) {
                    if (r.getReviewId().equals(review.getIdReview())) {

                        VBox replyBox = new VBox(5);
                        replyBox.setStyle("-fx-background-color: #f1f4f8;" +
                                "-fx-padding: 10;" +
                                "-fx-background-radius: 8;");

                        Label replyContent =
                                new Label("Therapist: " + r.getContent());
                        replyContent.setWrapText(true);

                        Label replyDate =
                                new Label(r.getCreatedAt().toString());
                        replyDate.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

                        replyBox.getChildren().addAll(replyContent, replyDate);

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

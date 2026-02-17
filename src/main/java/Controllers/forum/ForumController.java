package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import util.Session;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
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
    private TextArea contentField; // Liaison avec le FXML

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();

    private int currentUserId;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        if (Session.getInstance().getUser() != null) {
            currentUserId = Session.getInstance().getUser().getId();
        }

        loadReviews();
    }

    // ================= ADD REVIEW =================
    @FXML
    private void addReview(ActionEvent event) {
        String content = contentField.getText().trim();

        // Validation : non vide et minimum 10 caractères
        if (content.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Content cannot be empty!");
            return;
        }
        if (content.length() < 10) {
            showAlert(Alert.AlertType.WARNING, "Validation Error", "Content must be at least 10 characters!");
            return;
        }

        try {
            Review review = new Review();
            review.setContent(content);
            review.setIdUser(currentUserId);

            reviewService.create(review);

            contentField.clear(); // vide le champ
            loadReviews();        // recharge les reviews

            showAlert(Alert.AlertType.INFORMATION, "Success", "Review added successfully!");

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add review.");
        }
    }

    // ================= LOAD REVIEWS =================
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

    // ================= REVIEW CARD =================
    private VBox createReviewCard(Review review, List<ReviewReply> replies) {
        VBox card = new VBox(10);
        card.setStyle(
                "-fx-background-color:#F5F5DC;" +
                        "-fx-padding:15;" +
                        "-fx-background-radius:15;" +
                        "-fx-border-color:#A5D6A7;" +
                        "-fx-border-radius:15;"
        );

        Label date = new Label(review.getCreatedAt().toString());
        date.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

        Label content = new Label(review.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size:14px; -fx-text-fill:#2E7D32;");

        card.getChildren().addAll(date, content);

        // Ajouter les boutons Edit/Delete uniquement si c'est le client connecté
        if (review.getIdUser() == currentUserId) {
            HBox buttonBox = new HBox(10);

            Button editButton = new Button("Edit");
            editButton.setStyle("-fx-background-color:#FFF176; -fx-text-fill:#000;");
            editButton.setOnAction(e -> editReview(review));

            Button deleteButton = new Button("Delete");
            deleteButton.setStyle("-fx-background-color:#EF5350; -fx-text-fill:#fff;");
            deleteButton.setOnAction(e -> deleteReview(review));

            buttonBox.getChildren().addAll(editButton, deleteButton);
            card.getChildren().add(buttonBox);
        }

        // Ajouter les replies
        for (ReviewReply r : replies) {
            if (r.getReviewId().equals(review.getIdReview())) {
                VBox replyBox = createReplyBox(r);
                card.getChildren().add(replyBox);
            }
        }

        return card;
    }

    // ================= REPLY BOX =================
    private VBox createReplyBox(ReviewReply r) {
        VBox replyBox = new VBox(5);
        replyBox.setStyle(
                "-fx-background-color:#FAF3E0;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:12;" +
                        "-fx-border-color:#C8E6C9;" +
                        "-fx-border-radius:12;"
        );

        Label replyContent = new Label("Therapist: " + r.getContent());
        replyContent.setWrapText(true);
        replyContent.setStyle("-fx-text-fill:#2E7D32;");

        Label replyDate = new Label(r.getCreatedAt().toString());
        replyDate.setStyle("-fx-font-size:10px; -fx-text-fill:gray;");

        replyBox.getChildren().addAll(replyContent, replyDate);
        return replyBox;
    }

    // ================= EDIT REVIEW =================
    private void editReview(Review review) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Edit Review");
        confirm.setHeaderText("Do you really want to edit this review?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                TextInputDialog dialog = new TextInputDialog(review.getContent());
                dialog.setTitle("Edit Review");
                dialog.setHeaderText("Edit your review");
                dialog.setContentText("New content:");

                dialog.showAndWait().ifPresent(newContent -> {
                    if (newContent.trim().isEmpty() || newContent.trim().length() < 10) {
                        showAlert(Alert.AlertType.WARNING, "Validation Error", "Content must be at least 10 characters!");
                        return;
                    }
                    try {
                        review.setContent(newContent.trim());
                        reviewService.update(review);
                        loadReviews();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Review edited successfully!");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        showAlert(Alert.AlertType.ERROR, "Error", "Failed to edit review.");
                    }
                });
            }
        });
    }

    // ================= DELETE REVIEW =================
    private void deleteReview(Review review) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Review");
        confirm.setHeaderText("Do you really want to delete this review?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    reviewService.delete(review.getIdReview());
                    loadReviews();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Review deleted successfully!");
                } catch (SQLException e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete review.");
                }
            }
        });
    }

    // ================= HELPER METHOD =================
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

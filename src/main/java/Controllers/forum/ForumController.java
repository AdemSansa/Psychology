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
import java.util.Optional;
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
        styleAddBox();
        loadReviews();
    }

    // ================= STYLE ADD SECTION =================
    private void styleAddBox() {
        contentField.setStyle(
                "-fx-background-color:#F5F5DC;" +
                        "-fx-background-radius:15;" +
                        "-fx-border-color:#C8E6C9;" +
                        "-fx-border-radius:15;" +
                        "-fx-padding:10;"
        );
    }

    // ================= ADD REVIEW =================
    @FXML
    private void addReview() {

        if (contentField.getText().trim().isEmpty()) {
            showWarning("Missing Content",
                    "Please write something meaningful before submitting.");
            return;
        }

        try {
            Review review = new Review(contentField.getText(), currentUserId);
            reviewService.create(review);

            contentField.clear();
            loadReviews();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD REVIEWS =================
    private void loadReviews() {

        reviewContainer.getChildren().clear();

        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            for (Review review : reviews) {

                VBox card = createReviewCard(review);

                // ===== LOAD REPLIES =====
                for (ReviewReply r : replies) {

                    if (r.getReviewId().equals(review.getIdReview())) {
                        VBox replyBox = createReplyBox(r);
                        card.getChildren().add(replyBox);
                    }
                }

                reviewContainer.getChildren().add(card);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= REVIEW CARD =================
    private VBox createReviewCard(Review review) {

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

        HBox buttons = new HBox(10);

        Button replyBtn = styledGreenButton("Reply");
        Button editBtn = styledYellowButton("Edit");
        Button deleteBtn = styledRedButton("Delete");

        // DELETE REVIEW
        deleteBtn.setOnAction(e -> {
            if (confirmAction("Delete Message",
                    "Are you sure you want to delete this message?")) {
                try {
                    reviewService.delete(review.getIdReview());
                    loadReviews();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        // EDIT REVIEW
        editBtn.setOnAction(e -> {
            if (confirmAction("Edit Message",
                    "Do you really want to modify this message?")) {

                TextInputDialog dialog =
                        new TextInputDialog(review.getContent());
                dialog.setHeaderText("✏ Edit your message");

                dialog.showAndWait().ifPresent(newText -> {

                    if (newText.trim().isEmpty()) {
                        showWarning("Empty Content",
                                "Message cannot be empty.");
                        return;
                    }

                    try {
                        review.setContent(newText);
                        reviewService.update(review);
                        loadReviews();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });

        // REPLY BUTTON
        replyBtn.setOnAction(e -> showReplyInput(card, review));

        buttons.getChildren().addAll(replyBtn, editBtn, deleteBtn);
        card.getChildren().addAll(date, content, buttons);

        return card;
    }

    // ================= REPLY INPUT =================
    private void showReplyInput(VBox card, Review review) {

        VBox replyInputBox = new VBox(8);
        replyInputBox.setStyle(
                "-fx-background-color:#E8F5E9;" +
                        "-fx-padding:10;" +
                        "-fx-background-radius:12;"
        );

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Write a thoughtful reply...");
        replyArea.setStyle("-fx-background-radius:10;");

        Button sendReply = styledGreenButton("Send Reply");

        sendReply.setOnAction(ev -> {

            if (replyArea.getText().trim().isEmpty()) {
                showWarning("Empty Reply",
                        "Reply cannot be empty.");
                return;
            }

            try {
                ReviewReply reply = new ReviewReply(
                        replyArea.getText(),
                        review.getIdReview(),
                        therapistId
                );

                replyService.create(reply);
                loadReviews();

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        replyInputBox.getChildren().addAll(replyArea, sendReply);

        if (!card.getChildren().contains(replyInputBox)) {
            card.getChildren().add(replyInputBox);
        }
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

        Label replyContent =
                new Label("Therapist: " + r.getContent());
        replyContent.setWrapText(true);
        replyContent.setStyle("-fx-text-fill:#2E7D32;");

        Label replyDate =
                new Label(r.getCreatedAt().toString());
        replyDate.setStyle("-fx-font-size:10px; -fx-text-fill:gray;");

        HBox buttons = new HBox(10);

        Button editReply = styledYellowButton("Edit");
        Button deleteReply = styledRedButton("Delete");

        // EDIT REPLY
        editReply.setOnAction(ev -> {
            if (confirmAction("Edit Reply",
                    "Do you really want to modify this reply?")) {

                TextInputDialog dialog =
                        new TextInputDialog(r.getContent());
                dialog.setHeaderText("✏ Edit your reply");

                dialog.showAndWait().ifPresent(newText -> {

                    if (newText.trim().isEmpty()) {
                        showWarning("Empty Reply",
                                "Reply cannot be empty.");
                        return;
                    }

                    try {
                        r.setContent(newText);
                        replyService.update(r);
                        loadReviews();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            }
        });

        // DELETE REPLY
        deleteReply.setOnAction(ev -> {
            if (confirmAction("Delete Reply",
                    "Are you sure you want to delete this reply?")) {
                try {
                    replyService.delete(r.getIdReply());
                    loadReviews();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        });

        buttons.getChildren().addAll(editReply, deleteReply);
        replyBox.getChildren().addAll(replyContent, replyDate, buttons);

        return replyBox;
    }

    // ================= STYLED BUTTONS =================
    private Button styledGreenButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color:#81C784;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:15;"
        );
        return btn;
    }

    private Button styledYellowButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color:#FFF59D;" +
                        "-fx-background-radius:15;"
        );
        return btn;
    }

    private Button styledRedButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
                "-fx-background-color:#EF9A9A;" +
                        "-fx-text-fill:white;" +
                        "-fx-background-radius:15;"
        );
        return btn;
    }

    // ================= ALERTS =================
    private boolean confirmAction(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private void showWarning(String title, String message) {

        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

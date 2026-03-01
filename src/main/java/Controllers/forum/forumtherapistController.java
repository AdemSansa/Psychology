package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Entities.User;
import Service.ReviewService;
import Service.Reply_ReviewService;
import util.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class forumtherapistController implements Initializable {

    @FXML
    private VBox reviewContainer;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();

    private int currentTherapistId = 0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {


        reviewContainer.setStyle("-fx-background-color: #F5F5DC;");
        reviewContainer.setSpacing(15);
        reviewContainer.setPadding(new Insets(20));

        User user = Session.getInstance().getUser();

        if (user != null && user.getRole().equals("therapist")) {
            currentTherapistId = user.getId();
        }

        loadReviews();
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
        card.setStyle("-fx-background-color: #FAF3E0; -fx-padding:15; "
                + "-fx-background-radius:15; -fx-border-radius:15;");
        card.setPadding(new Insets(10));

        Label content = new Label(review.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size:14px; -fx-text-fill:#5D4037;");

        Button replyBtn = new Button("💬 Reply");
        replyBtn.setStyle("-fx-background-color:#A5D6A7; -fx-text-fill:white; "
                + "-fx-background-radius:20; -fx-padding:5 15 5 15;");

        replyBtn.setOnAction(e -> {

            User user = Session.getInstance().getUser();

            if (user == null || !user.getRole().equals("therapist")) {
                showWarning("Access denied", "Only therapists can reply.");
                return;
            }

            showReplyInput(card, review);
        });

        card.getChildren().addAll(content, replyBtn);

        for (ReviewReply r : replies) {
            if (r.getReviewId() == review.getIdReview()) {
                card.getChildren().add(createReplyBox(r));
            }
        }

        return card;
    }

    private void showReplyInput(VBox card, Review review) {

        VBox inputBox = new VBox(8);

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Write your reply...");
        replyArea.setStyle("-fx-background-radius:10;");

        Button send = new Button("📨 Send");
        send.setStyle("-fx-background-color:#81C784; -fx-text-fill:white; "
                + "-fx-background-radius:20; -fx-padding:5 15 5 15;");

        send.setOnAction(e -> {

            User user = Session.getInstance().getUser();

            if (user == null || !user.getRole().equals("therapist")) {
                showWarning("Access denied", "Only therapists can reply.");
                return;
            }

            String text = replyArea.getText().trim();


            if (text.isEmpty()) {
                showWarning("Empty reply", "Reply cannot be empty.");
                return;
            }

            if (text.length() < 10) {
                showWarning("Too short", "Reply must contain at least 10 characters.");
                return;
            }

            try {
                ReviewReply reply = new ReviewReply(
                        text,
                        review.getIdReview(),
                        user.getId()
                );

                replyService.create(reply);
                loadReviews();
                showSuccess("Reply added successfully!");

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        inputBox.getChildren().addAll(replyArea, send);
        card.getChildren().add(inputBox);
    }

    private VBox createReplyBox(ReviewReply r) {

        VBox box = new VBox(5);
        box.setStyle("-fx-background-color:#FFF8E1; -fx-padding:10; "
                + "-fx-background-radius:12;");
        box.setPadding(new Insets(8));

        Label content = new Label("👩‍⚕️ " + r.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-text-fill:#4E342E;");

        box.getChildren().add(content);

        if (r.getIdTherapist() == currentTherapistId) {


            HBox buttonBox = new HBox(10);

            Button edit = new Button("✏ Edit");
            edit.setStyle("-fx-background-color:#FFE082; -fx-background-radius:20;");

            Button delete = new Button("🗑 Delete");
            delete.setStyle("-fx-background-color:#EF9A9A; -fx-text-fill:white; "
                    + "-fx-background-radius:20;");


            edit.setOnAction(e -> {

                if (!confirmAction("Edit", "Do you really want to edit this reply?"))
                    return;

                TextInputDialog dialog = new TextInputDialog(r.getContent());
                dialog.setHeaderText("Edit your reply");

                dialog.showAndWait().ifPresent(newText -> {

                    String updated = newText.trim();

                    if (updated.isEmpty()) {
                        showWarning("Empty", "Reply cannot be empty.");
                        return;
                    }

                    try {
                        r.setContent(updated);
                        replyService.update(r);
                        loadReviews();
                        showSuccess("Reply updated successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });
            });


            delete.setOnAction(e -> {

                if (confirmAction("Delete", "Do you really want to delete this reply?")) {
                    try {
                        replyService.delete(r.getIdReply());
                        loadReviews();
                        showSuccess("Reply deleted successfully!");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                }
            });

            buttonBox.getChildren().addAll(edit, delete);
            box.getChildren().add(buttonBox);
        }

        return box;
    }

    private boolean confirmAction(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        Optional<ButtonType> res = alert.showAndWait();
        return res.isPresent() && res.get() == ButtonType.OK;
    }

    private void showWarning(String t, String m) {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle(t);
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }

    private void showSuccess(String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle("Success");
        a.setHeaderText(null);
        a.setContentText(m);
        a.showAndWait();
    }
}

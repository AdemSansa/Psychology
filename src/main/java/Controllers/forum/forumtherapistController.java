package Controllers.forum;
import Entities.Therapistis;
import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import util.Session;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.*;

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

    private int currentTherapistId;

    @Override


    public void initialize(URL url, ResourceBundle rb) {
        // On ne vérifie plus si le thérapeute est null
        // On récupère l'ID du thérapeute (0 si non défini)
        currentTherapistId = Session.getInstance().getTherapist() != null
                ? Session.getInstance().getTherapist().getId()
                : 0;

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
        card.setStyle("-fx-background-color:#F5F5DC; -fx-padding:15; -fx-background-radius:15; -fx-border-color:#A5D6A7; -fx-border-radius:15;");

        Label date = new Label(review.getCreatedAt().toString());
        date.setStyle("-fx-text-fill:gray; -fx-font-size:11px;");

        Label content = new Label(review.getContent());
        content.setWrapText(true);
        content.setStyle("-fx-font-size:14px; -fx-text-fill:#2E7D32;");

        // Ajouter Reply Button
        Button replyBtn = styledGreenButton("Reply");
        replyBtn.setOnAction(e -> showReplyInput(card, review));

        card.getChildren().addAll(date, content, replyBtn);

        // Ajouter replies
        for (ReviewReply r : replies) {
            if (r.getReviewId().equals(review.getIdReview())) {
                VBox replyBox = createReplyBox(r);
                card.getChildren().add(replyBox);
            }
        }

        return card;
    }

    private void showReplyInput(VBox card, Review review) {
        VBox replyInputBox = new VBox(8);
        replyInputBox.setStyle("-fx-background-color:#E8F5E9; -fx-padding:10; -fx-background-radius:12;");

        TextArea replyArea = new TextArea();
        replyArea.setPromptText("Write your reply...");
        replyArea.setStyle("-fx-background-radius:10;");

        Button sendReply = styledGreenButton("Send Reply");
        sendReply.setOnAction(ev -> {
            String text = replyArea.getText().trim();

            // 1. Session check
            Therapistis therapist = Session.getInstance().getTherapist();
            if (therapist == null) {
                showWarning("Not logged in", "You must be logged in as a therapist to reply.");
                return;
            }
            int therapistId = therapist.getId();

            // 2. Content validation
            if (text.isEmpty() || text.length() < 10) {
                showWarning("Invalid reply", "Reply must contain at least 10 characters.");
                return;
            }

            // 3. Insertion
            try {
                ReviewReply reply = new ReviewReply(text, review.getIdReview(), therapistId);
                replyService.create(reply);
                loadReviews();
                showSuccess("Reply added!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                showWarning("Error", "Unable to add reply.");
            }
        });

        replyInputBox.getChildren().addAll(replyArea, sendReply);
        if (!card.getChildren().contains(replyInputBox)) {
            card.getChildren().add(replyInputBox);
        }
    }

    private VBox createReplyBox(ReviewReply r) {
        VBox replyBox = new VBox(5);
        replyBox.setStyle("-fx-background-color:#FAF3E0; -fx-padding:10; -fx-background-radius:12; -fx-border-color:#C8E6C9; -fx-border-radius:12;");

        Label replyContent = new Label("Therapist: " + r.getContent());
        replyContent.setWrapText(true);
        replyContent.setStyle("-fx-text-fill:#2E7D32;");

        Label replyDate = new Label(r.getCreatedAt().toString());
        replyDate.setStyle("-fx-font-size:10px; -fx-text-fill:gray;");

        HBox buttons = new HBox(10);
        if (r.getIdTherapist() == currentTherapistId) {
            Button edit = styledYellowButton("Edit");
            Button delete = styledRedButton("Delete");

            edit.setOnAction(e -> {
                TextInputDialog dialog = new TextInputDialog(r.getContent());
                dialog.setHeaderText("Edit your reply");
                dialog.showAndWait().ifPresent(newText -> {
                    String updated = newText.trim();
                    if (updated.length() < 10) { showWarning("Too short", "Reply must have at least 10 characters."); return; }
                    try { r.setContent(updated); replyService.update(r); loadReviews(); showSuccess("Reply updated!"); } catch (SQLException ex) { ex.printStackTrace(); }
                });
            });

            delete.setOnAction(e -> {
                if (confirmAction("Delete", "Delete this reply?")) {
                    try { replyService.delete(r.getIdReply()); loadReviews(); showSuccess("Reply deleted!"); } catch (SQLException ex) { ex.printStackTrace(); }
                }
            });

            buttons.getChildren().addAll(edit, delete);
            replyBox.getChildren().addAll(replyContent, replyDate, buttons);
        } else {
            replyBox.getChildren().addAll(replyContent, replyDate);
        }

        return replyBox;
    }

    private Button styledGreenButton(String text) { Button btn = new Button(text); btn.setStyle("-fx-background-color:#81C784; -fx-text-fill:white; -fx-background-radius:15;"); return btn; }
    private Button styledYellowButton(String text) { Button btn = new Button(text); btn.setStyle("-fx-background-color:#FFF59D; -fx-background-radius:15;"); return btn; }
    private Button styledRedButton(String text) { Button btn = new Button(text); btn.setStyle("-fx-background-color:#EF9A9A; -fx-text-fill:white; -fx-background-radius:15;"); return btn; }

    private boolean confirmAction(String title, String message) { Alert alert = new Alert(Alert.AlertType.CONFIRMATION); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); Optional<ButtonType> res = alert.showAndWait(); return res.isPresent() && res.get() == ButtonType.OK; }
    private void showWarning(String t, String m) { Alert a = new Alert(Alert.AlertType.WARNING); a.setTitle(t); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
    private void showSuccess(String m) { Alert a = new Alert(Alert.AlertType.INFORMATION); a.setTitle("Success"); a.setHeaderText(null); a.setContentText(m); a.showAndWait(); }
}

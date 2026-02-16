package Controllers.forum;

import Entities.Review;
import Service.ReviewService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class EditReviewController {

    @FXML
    private TextArea contentField;

    private Review review;
    private final ReviewService reviewService = new ReviewService();

    public void setReview(Review review) {
        this.review = review;
        contentField.setText(review.getContent());
    }

    @FXML
    private void updateReview() {

        if (review == null) {
            showError("No review selected.");
            return;
        }

        if (contentField.getText().trim().isEmpty()) {
            showWarning("Content cannot be empty.");
            return;
        }

        try {
            review.setContent(contentField.getText());
            reviewService.update(review);

            showSuccess("Review modified successfully.");

        } catch (Exception e) {
            showError("Error while updating review.");
            e.printStackTrace();
        }
    }

    // ================= SUCCESS ALERT =================
    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= WARNING ALERT =================
    private void showWarning(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Warning");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ================= ERROR ALERT =================
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

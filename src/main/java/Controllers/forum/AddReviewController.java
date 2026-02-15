package Controllers.forum;

import Entities.Review;
import Service.ReviewService;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;

public class AddReviewController {

    @FXML
    private TextArea contentField;

    private final ReviewService reviewService = new ReviewService();
    private final int currentUserId = 1;

    @FXML
    private void addReview() {

        if (contentField.getText().trim().isEmpty()) {
            showWarning("Content cannot be empty.");
            return;
        }

        try {
            Review review = new Review(contentField.getText(), currentUserId);
            reviewService.create(review);

            contentField.clear();
            showSuccess("Review added successfully.");

        } catch (Exception e) {
            showError("Error while adding review.");
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

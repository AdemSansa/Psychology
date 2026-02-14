package Controllers.forum;

import Entities.Review;
import Service.ReviewService;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class AddReviewController {

    @FXML
    private TextArea contentField;

    private final ReviewService reviewService = new ReviewService();
    private final int currentUserId = 1;

    @FXML
    private void addReview() {
        try {
            Review review = new Review(contentField.getText(), currentUserId);
            reviewService.create(review);
            contentField.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

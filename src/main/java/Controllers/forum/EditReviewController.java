package Controllers.forum;

import Entities.Review;
import Service.ReviewService;
import javafx.fxml.FXML;
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
        try {
            review.setContent(contentField.getText());
            reviewService.update(review);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

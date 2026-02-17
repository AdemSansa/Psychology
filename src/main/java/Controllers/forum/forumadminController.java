package Controllers.forum;

import Entities.Review;
import Entities.ReviewReply;
import Service.ReviewService;
import Service.Reply_ReviewService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class forumadminController implements Initializable {

    @FXML
    private TableView<Review> tableReviews;

    private final ReviewService reviewService = new ReviewService();
    private final Reply_ReviewService replyService = new Reply_ReviewService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadReviews();
    }

    private void loadReviews() {
        try {
            List<Review> reviews = reviewService.list();
            List<ReviewReply> replies = replyService.list();

            tableReviews.getItems().clear();
            for (Review r : reviews) {
                tableReviews.getItems().add(r);
                // Possibilité d’afficher les replies dans une colonne ou un VBox pour chaque
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

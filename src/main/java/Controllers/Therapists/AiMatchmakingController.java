package Controllers.Therapists;

import Entities.Therapistis;
import Service.AIMatchmakingService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.io.File;
import java.util.List;

public class AiMatchmakingController {

    @FXML
    private TextArea patientMessageInput;

    @FXML
    private Button findButton;

    @FXML
    private VBox resultContainer;

    @FXML
    private ImageView therapistImageView;

    @FXML
    private Label therapistNameLabel;

    @FXML
    private Label specializationLabel;

    @FXML
    private Label slotLabel;

    @FXML
    private Label reasoningLabel;

    @FXML
    private HBox booksContainer;

    private AIMatchmakingService aiService;
    private Therapistis currentlyRecommendedTherapist;

    @FXML
    public void initialize() {
        aiService = new AIMatchmakingService();
    }

    @FXML
    void handleBack(ActionEvent event) {
        util.SceneManager.switchScene("/com/example/psy/intro/Home.fxml");
    }

    @FXML
    void handleFindMatch(ActionEvent event) {
        String patientMessage = patientMessageInput.getText().trim();
        if (patientMessage.isEmpty()) {
            return;
        }

        // Processing state
        findButton.setDisable(true);
        findButton.setText("AI Analysis in progress...");
        resultContainer.setVisible(false);

        Task<AIMatchmakingService.MatchmakingResult> task = new Task<>() {
            @Override
            protected AIMatchmakingService.MatchmakingResult call() throws Exception {
                return aiService.findBestTherapist(patientMessage);
            }
        };

        task.setOnSucceeded(e -> {
            findButton.setDisable(false);
            findButton.setText("Analyze my needs ✨");
            AIMatchmakingService.MatchmakingResult result = task.getValue();
            displayResult(result);
        });

        task.setOnFailed(e -> {
            findButton.setDisable(false);
            findButton.setText("Analyze my needs ✨");
            Throwable error = task.getException();
            error.printStackTrace();
        });

        Thread th = new Thread(task);
        th.setDaemon(true);
        th.start();
    }

    private void displayResult(AIMatchmakingService.MatchmakingResult result) {
        this.currentlyRecommendedTherapist = result.getTherapist();

        therapistNameLabel.setText(
                currentlyRecommendedTherapist.getFirstName() + " " + currentlyRecommendedTherapist.getLastName());
        specializationLabel.setText(currentlyRecommendedTherapist.getSpecialization());
        reasoningLabel.setText(result.getExplanation());
        slotLabel.setText(result.getSuggestedSlot());

        // Load Therapist Photo
        String photoUrl = currentlyRecommendedTherapist.getPhotoUrl();
        if (photoUrl != null && !photoUrl.isEmpty()) {
            try {
                // Try to load as a file first (since it's a desktop app)
                File file = new File(photoUrl);
                if (file.exists()) {
                    therapistImageView.setImage(new Image(file.toURI().toString()));
                } else if (photoUrl.startsWith("http")) {
                    therapistImageView.setImage(new Image(photoUrl));
                } else {
                    // Try to load from resources if it's just a filename
                    String resPath = "/com/example/psy/media/" + photoUrl;
                    if (getClass().getResource(resPath) != null) {
                        therapistImageView.setImage(new Image(getClass().getResourceAsStream(resPath)));
                    }
                }
            } catch (Exception e) {
                System.err.println("Could not load therapist photo: " + e.getMessage());
            }
        }

        resultContainer.setVisible(true);
        displayBooks(result.getRecommendedBooks());
    }

    private void displayBooks(List<AIMatchmakingService.Book> books) {
        if (booksContainer == null) {
            System.err.println("Error: booksContainer is NULL!");
            return;
        }
        System.out.println("displayBooks called with " + books.size() + " books.");
        booksContainer.getChildren().clear();

        if (books.isEmpty()) {
            Label noBooks = new Label("No specific reading recommendations for this session.");
            noBooks.setStyle("-fx-text-fill: #7F8F69; -fx-font-style: italic; -fx-font-size: 14px;");
            booksContainer.getChildren().add(noBooks);
            return;
        }

        for (AIMatchmakingService.Book book : books) {
            VBox bookCard = new VBox(5);
            bookCard.setAlignment(javafx.geometry.Pos.TOP_CENTER);
            bookCard.setPrefWidth(130);
            bookCard.setMinHeight(210);
            bookCard.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 12; " +
                    "-fx-border-color: rgba(127, 143, 105, 0.2); -fx-border-width: 1; -fx-border-radius: 12; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.04), 8, 0, 0, 3);");

            ImageView coverView = new ImageView();
            coverView.setFitHeight(140);
            coverView.setFitWidth(90);
            coverView.setPreserveRatio(true);

            // Background loading image
            try {
                Image img = new Image(book.getCoverUrl(), true);
                coverView.setImage(img);
            } catch (Exception e) {
                // Ignore image load errors
            }

            Label titleLabel = new Label(book.getTitle());
            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #2B322E;");
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(110);
            titleLabel.setAlignment(javafx.geometry.Pos.CENTER);
            titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

            Label authorLabel = new Label(book.getAuthor());
            authorLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7F8F69;");
            authorLabel.setWrapText(true);
            authorLabel.setMaxWidth(110);

            bookCard.getChildren().addAll(coverView, titleLabel, authorLabel);
            booksContainer.getChildren().add(bookCard);
        }
    }

    @FXML
    void handleViewProfile(ActionEvent event) {
        if (currentlyRecommendedTherapist != null) {
            System.out.println("Navigating to therapist profile ID: " + currentlyRecommendedTherapist.getId());
        }
    }

    @FXML
    void handleBookAppointment(ActionEvent event) {
        if (currentlyRecommendedTherapist != null) {
            System.out.println(
                    "Navigating to booking with therapist ID: " + currentlyRecommendedTherapist.getId());
        }
    }
}

package Controllers.QuizAssesment;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import util.SceneManager;

public class QuizCinematicController {

    @FXML
    private Circle bgCircle1;

    @FXML
    private Circle bgCircle2;

    @FXML
    private HBox contentBox;

    @FXML
    private ImageView mascotImage;

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label subtitleLabel;

    @FXML
    private Button takeQuizButton;

    @FXML
    private Button skipButton;

    @FXML
    public void initialize() {
        // Initial state before animation
        contentBox.setOpacity(0.0);
        contentBox.setScaleX(0.9);
        contentBox.setScaleY(0.9);

        mascotImage.setOpacity(0.0);
        mascotImage.setTranslateY(50.0); // Start slightly lower for slide-up

        takeQuizButton.setOpacity(0.0);
        skipButton.setOpacity(0.0);

        // Start background ambient animations
        animateBackgroundShapes();

        // Main Entrance Animation
        playEntranceAnimation();
    }

    private void animateBackgroundShapes() {
        TranslateTransition tt1 = new TranslateTransition(Duration.seconds(10), bgCircle1);
        tt1.setByX(50);
        tt1.setByY(30);
        tt1.setAutoReverse(true);
        tt1.setCycleCount(TranslateTransition.INDEFINITE);
        tt1.play();

        TranslateTransition tt2 = new TranslateTransition(Duration.seconds(12), bgCircle2);
        tt2.setByX(-60);
        tt2.setByY(-40);
        tt2.setAutoReverse(true);
        tt2.setCycleCount(TranslateTransition.INDEFINITE);
        tt2.play();
    }

    private void playEntranceAnimation() {
        FadeTransition ftContent = new FadeTransition(Duration.millis(1200), contentBox);
        ftContent.setFromValue(0.0);
        ftContent.setToValue(1.0);

        ScaleTransition stContent = new ScaleTransition(Duration.millis(1200), contentBox);
        stContent.setFromX(0.9);
        stContent.setFromY(0.9);
        stContent.setToX(1.0);
        stContent.setToY(1.0);

        FadeTransition ftBtn1 = new FadeTransition(Duration.millis(800), takeQuizButton);
        ftBtn1.setFromValue(0.0);
        ftBtn1.setToValue(1.0);
        ftBtn1.setDelay(Duration.millis(800)); // Play slightly after main box appears

        FadeTransition ftBtn2 = new FadeTransition(Duration.millis(800), skipButton);
        ftBtn2.setFromValue(0.0);
        ftBtn2.setToValue(1.0);
        ftBtn2.setDelay(Duration.millis(1000)); // Play after main button

        // Mascot image entrance (Slide Up & Fade In)
        FadeTransition ftImage = new FadeTransition(Duration.millis(1000), mascotImage);
        ftImage.setFromValue(0.0);
        ftImage.setToValue(1.0);
        ftImage.setDelay(Duration.millis(400));

        TranslateTransition ttImage = new TranslateTransition(Duration.millis(1000), mascotImage);
        ttImage.setFromY(50.0);
        ttImage.setToY(0.0);
        ttImage.setDelay(Duration.millis(400));

        ftContent.play();
        stContent.play();
        ftImage.play();
        ttImage.play();
        ftBtn1.play();
        ftBtn2.play();
    }

    @FXML
    private void handleTakeQuiz() {
        // Go to Quiz Assessment List
        SceneManager.loadPage("/com/example/psy/QuizAssesment/quizList.fxml");
    }

    @FXML
    private void handleSkip() {
        // Go to standard Dashboard
        SceneManager.loadPage("/com/example/psy/intro/dashboard.fxml");
    }
}

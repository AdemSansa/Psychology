package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    private static StackPane contentArea;

    public static void setContentArea(StackPane pane) {
        contentArea = pane;
    }

    public static void switchScene(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            StackPane rootContainer = new StackPane();
            // Load global CSS to ensure programmatically added nodes (like the AI button)
            // are styled
            rootContainer.getStylesheets()
                    .add(SceneManager.class.getResource("/com/example/psy/style.css").toExternalForm());

            rootContainer.getChildren().add(root);
            addFloatingAIButton(rootContainer);

            Scene scene = new Scene(rootContainer);
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load scene: " + fxmlPath, e);
        }
    }

    public static <T> T switchSceneWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            StackPane rootContainer = new StackPane();
            // Load global CSS to ensure programmatically added nodes (like the AI button)
            // are styled
            rootContainer.getStylesheets()
                    .add(SceneManager.class.getResource("/com/example/psy/style.css").toExternalForm());

            rootContainer.getChildren().add(root);
            addFloatingAIButton(rootContainer);

            Scene scene = new Scene(rootContainer);
            stage.setScene(scene);
            stage.setFullScreen(true);
            stage.show();

            return loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void loadPage(String fxmlPath) {
        try {
            if (contentArea == null) {
                throw new IllegalStateException("ContentArea not set!");
            }

            Node page = FXMLLoader.load(SceneManager.class.getResource(fxmlPath));

            contentArea.getChildren().setAll(page);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T loadPageWithController(String fxmlPath) {
        try {
            if (contentArea == null) {
                throw new IllegalStateException("ContentArea not set!");
            }

            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Node page = loader.load();

            contentArea.getChildren().setAll(page);

            return loader.getController();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void goBack() {
    }

    private static void addFloatingAIButton(StackPane rootContainer) {
        // Only show if a user is logged in
        if (util.Session.getInstance().getUser() == null) {
            return;
        }

        Button aiButton = new Button(""); // Empty text, will use CSS background-image
        aiButton.getStyleClass().add("floating-ai-button");

        // Position at bottom right
        StackPane.setAlignment(aiButton, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(aiButton, new Insets(0, 30, 30, 0)); // Padding from corner

        // On click, navigate to AI matching view
        aiButton.setOnAction(e -> {
            switchScene("/com/example/psy/Therapist/ai_matchmaking.fxml");
        });

        rootContainer.getChildren().add(aiButton);
    }
}

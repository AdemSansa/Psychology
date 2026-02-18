package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

            stage.setScene(new Scene(root));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static <T> T switchSceneWithController(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(fxmlPath));
            Parent root = loader.load();

            stage.setScene(new Scene(root));
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
}

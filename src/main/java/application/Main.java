// java
package application;

import Database.dbconnect;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import util.SceneManager;

import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws  Exception {
        URL fxmlUrl = getClass().getResource("/com/example/psy/auth/login.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML introuvable: /com/example/psy/login.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setTitle("Slimenify");
        stage.setScene(scene);
        stage.show();

        SceneManager.setStage(stage);
        Connection conn = dbconnect.getInstance().getConnection();


    }

    public static void main(String[] args) {
        launch(args);
    }
}

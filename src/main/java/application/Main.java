package application;

import Database.dbconnect;
import javafx.application.Application;
import javafx.stage.Stage;
import util.SceneManager;

import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws  Exception {
        // Give stage to SceneManager
        SceneManager.setStage(stage);

        // Load first page using SceneManager (IMPORTANT for Back)
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");

        stage.setTitle("Slimenify");
        stage.show();

        // Database connection
        Connection conn = dbconnect.getInstance().getConnection();

      
    }

    public static void main(String[] args) {
        launch(args);
    }
}

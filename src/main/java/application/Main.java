package application;

import Database.dbconnect;
import javafx.application.Application;
import javafx.stage.Stage;
import util.SceneManager;

import java.sql.Connection;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws  Exception {

        SceneManager.setStage(stage);
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");

        stage.setTitle("Slimenify");
        stage.setMaximized(true); // open application in full screen (maximized window)
        stage.show();


        Connection conn = dbconnect.getInstance().getConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

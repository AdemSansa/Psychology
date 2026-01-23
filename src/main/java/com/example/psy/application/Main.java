// java
package com.example.psy.application;

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
    public void start(Stage stage) throws Exception {
        URL fxmlUrl = getClass().getResource("/com/example/psy/login.fxml");
        if (fxmlUrl == null) {
            throw new IllegalStateException("FXML introuvable: /com/example/psy/login.fxml");
        }

        FXMLLoader loader = new FXMLLoader(fxmlUrl);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        stage.setTitle("Futuristic Login");
        stage.setScene(scene);
        stage.show();

        SceneManager.setStage(stage);
        try (Connection conn = dbconnect.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Connexion DB OK");
            } else {
                System.err.println("Connexion DB null ou fermée");
            }
        } catch (SQLException e) {
            System.err.println("Échec de la connexion : " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}

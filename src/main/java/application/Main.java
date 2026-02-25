package application;

import Database.dbconnect;
import javafx.application.Application;
import javafx.stage.Stage;
import util.SceneManager;

import java.sql.Connection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import Service.EmailReminderService;

public class Main extends Application {

    private ScheduledExecutorService scheduler;

    @Override
    public void start(Stage stage) throws Exception {

        SceneManager.setStage(stage);
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");

        stage.setTitle("Slimenify");
        stage.show();

        Connection conn = dbconnect.getInstance().getConnection();

        // Start background email reminder timer
        scheduler = Executors.newSingleThreadScheduledExecutor();
        // Delay of 0 means the emails send immediately upon opening the app
        // The 1 TimeUnit.DAYS means it runs every 24 hours while the app is open
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Running background email reminder check...");
                new EmailReminderService().sendRemindersForTomorrow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 0, 1, TimeUnit.DAYS);
    }

    @Override
    public void stop() throws Exception {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}

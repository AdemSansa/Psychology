package application;

import Database.dbconnect;
import Service.EmailReminderService;
import javafx.application.Application;
import javafx.stage.Stage;
import util.SceneManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main extends Application {

    private ScheduledExecutorService scheduler;

    @Override
    public void start(Stage stage) throws Exception {

        // Initialize JavaFX
        SceneManager.setStage(stage);
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
        stage.setTitle("Slimenify");
        stage.show();

        // Initialize DB connection once (just for testing, optional)
        dbconnect.getInstance().getConnection();

        // Start background email reminder scheduler
        scheduler = Executors.newSingleThreadScheduledExecutor();

        // Compute initial delay to run at 8 AM
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(12).withMinute(48).withSecond(38).withNano(0);
        if (now.compareTo(nextRun) >= 0) {
            nextRun = nextRun.plusDays(1); // schedule for tomorrow if past 8 AM
        }
        long initialDelay = Duration.between(now, nextRun).toSeconds();

        long period = 24 * 60 * 60; // 24 hours in seconds

        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Running background email reminder check...");
                new EmailReminderService().sendRemindersForTomorrow();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, initialDelay, period, TimeUnit.SECONDS);

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
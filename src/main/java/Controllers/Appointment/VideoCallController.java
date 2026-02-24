package Controllers.Appointment;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.io.IOException;

public class VideoCallController {

    @FXML
    private WebView webView;

    private String meetingLink;
    private Stage stage;

    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
        loadMeeting();
    }

    private void loadMeeting() {
        WebEngine engine = webView.getEngine();
        engine.load(meetingLink);
    }
    public static VideoCallController openVideoCall(String meetingLink) {
        try {
            FXMLLoader loader = new FXMLLoader(VideoCallController.class.getResource(
                    "/com/example/psy/Appointment/VideoCallPopup.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Video Call");

            VideoCallController controller = loader.getController();
            controller.stage = stage; // store stage reference
            controller.setMeetingLink(meetingLink);

            stage.show();
            return controller;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close() {
        if (stage != null) {
            stage.close();
        }
    }
}
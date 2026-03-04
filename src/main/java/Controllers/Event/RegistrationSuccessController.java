package Controllers.Event;

import Entities.Registration;
import Service.RegistrationService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import util.SceneManager;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class RegistrationSuccessController {

    @FXML private Label chairNumberLabel;
    @FXML private ImageView qrImageView;
    @FXML private Label qrLoadingLabel;
    @FXML private StackPane qrContainer;

    private final RegistrationService service = new RegistrationService();

    public void setData(Registration r, int registrationId) {
        // 1. Get chair number
        int chairNum = service.getChairNumber(registrationId, r.getEventId());
        chairNumberLabel.setText(String.format("#%02d", chairNum));

        // 2. Generate content for the QR Code
        String qrContent = "For more Details Visit https://www.psychologies.com/";

        // 3. Generate QR Code via API
        generateQR(qrContent);
    }

    private void generateQR(String data) {
        Task<Image> qrTask = new Task<>() {
            @Override
            protected Image call() throws Exception {
                // Using GoQR.me API for 100% reliability and clean code
                String encodedData = URLEncoder.encode(data, StandardCharsets.UTF_8);
                String apiUrl = "https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=" + encodedData;
                
                // JavaFX Image can load directly from a URL string
                return new Image(apiUrl, true); // true for background loading
            }
        };

        qrTask.setOnSucceeded(e -> {
            Image qrImage = qrTask.getValue();
            if (qrImage.isError()) {
                qrLoadingLabel.setText("❌ Fin de quota ou erreur réseau");
            } else {
                qrImageView.setImage(qrImage);
                qrLoadingLabel.setVisible(false);
            }
        });

        qrTask.setOnFailed(e -> {
            qrLoadingLabel.setText("❌ QR generation failed");
        });

        new Thread(qrTask).start();
    }

    @FXML
    private void handleBack() {
        SceneManager.loadPage("/com/example/psy/Event/events.fxml");
    }
}

package Controllers.Event;

import Entities.Registration;
import Service.RegistrationService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import util.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;

public class RegistrationListController implements Initializable {

    @FXML private FlowPane cardContainer;
    @FXML private Label totalLabel;

    private final RegistrationService service = new RegistrationService();
    private ObservableList<Registration> registrations = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadRegistrations();
    }

    // ================= LOAD =================
    private void loadRegistrations() {
        try {
            registrations.setAll(service.list());
            renderCards();
            totalLabel.setText("Total registrations: " + registrations.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CARD =================
    private VBox createCard(Registration r) {

        Label name = new Label("👤 Participant: " + r.getParticipantName());
        Label event = new Label("📅 Event ID: " + r.getEventId());
        Label status = new Label("Status: " + r.getStatus());
        Label qr = new Label("QR: " + r.getQrCode());

        status.setStyle(getStatusColor(r.getStatus()));

        // ===== EDIT =====
        Button edit = new Button("Edit");
        edit.getStyleClass().add("btn-primary");

        edit.setOnAction(e -> showEditDialog(r));

        // ===== DELETE =====
        Button delete = new Button("Delete");
        delete.getStyleClass().add("btn-danger");

        delete.setOnAction(e -> {
            try {
                service.delete(r.getIdRegistration());
                loadRegistrations();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // ===== BUTTON ROW =====
        HBox btnRow = new HBox(10, edit, delete);
        btnRow.setAlignment(Pos.CENTER);

        VBox card = new VBox(8, name, event, status, qr, btnRow);
        card.setPrefWidth(250);
        card.setAlignment(Pos.CENTER_LEFT);
        card.getStyleClass().add("event-card");

        return card;
    }

    // ================= EDIT DIALOG =================
    private void showEditDialog(Registration r) {

        TextField nameField = new TextField(r.getParticipantName());

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("registered", "cancelled", "attended");
        statusBox.setValue(r.getStatus());

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Registration");

        VBox content = new VBox(10,
                new Label("Participant Name"),
                nameField,
                new Label("Status"),
                statusBox
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                r.setParticipantName(nameField.getText());
                r.setStatus(statusBox.getValue());

                service.update(r);
                loadRegistrations();

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // ================= RENDER =================
    private void renderCards() {
        cardContainer.getChildren().clear();
        for (Registration r : registrations) {
            cardContainer.getChildren().add(createCard(r));
        }
    }

    // ================= STATUS COLOR =================
    private String getStatusColor(String s) {
        switch (s) {
            case "registered":
                return "-fx-text-fill: green; -fx-font-weight:bold;";
            case "cancelled":
                return "-fx-text-fill: red; -fx-font-weight:bold;";
            default:
                return "-fx-text-fill: orange; -fx-font-weight:bold;";
        }
    }

    // ================= BACK =================
    @FXML
    private void handleBack() {
        SceneManager.switchScene("/com/example/psy/Event/events.fxml");
    }
}

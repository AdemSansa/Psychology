package Controllers.Event;

import Entities.Registration;
import Service.RegistrationService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import util.SceneManager;

import java.sql.SQLException;
import java.util.*;

public class RegistrationController {

    @FXML private Label eventInfoLabel;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> statusBox;
    @FXML private FlowPane cardContainer;

    private final RegistrationService service = new RegistrationService();
    private ObservableList<Registration> list = FXCollections.observableArrayList();

    private int eventId;
    private Registration selected = null;

    @FXML
    public void initialize() {
        statusBox.getItems().addAll("registered", "cancelled", "attended");
        statusBox.setValue("registered");
    }

    public void setEventId(int id) {
        this.eventId = id;
        eventInfoLabel.setText("Participants of Event ID: " + id);
        loadParticipants();
    }

    private void loadParticipants() {
        try {
            list.setAll(service.listByEvent(eventId));
            renderCards();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleRegister() {

        try {
            if (nameField.getText().isEmpty()) {
                alert("Enter name");
                return;
            }

            if (selected == null) {
                Registration r = new Registration();
                r.setEventId(eventId);
                r.setParticipantName(nameField.getText());
                r.setStatus(statusBox.getValue());
                r.setQrCode(UUID.randomUUID().toString());

                service.create(r);
                alert("Participant added");
            } else {
                selected.setParticipantName(nameField.getText());
                selected.setStatus(statusBox.getValue());
                service.update(selected);
                alert("Participant updated");
                selected = null;
            }

            nameField.clear();
            loadParticipants();

        } catch (SQLException e) {
            alert(e.getMessage());
        }
    }

    private VBox createCard(Registration r) {

        Label name = new Label("👤 " + r.getParticipantName());
        Label status = new Label("Status: " + r.getStatus());

        Button edit = new Button("Edit");
        edit.getStyleClass().add("btn-primary");

        edit.setOnAction(e -> {
            selected = r;
            nameField.setText(r.getParticipantName());
            statusBox.setValue(r.getStatus());
        });

        Button delete = new Button("Delete");
        delete.getStyleClass().add("btn-danger");

        delete.setOnAction(e -> {
            try {
                service.delete(r.getIdRegistration());
                loadParticipants();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        VBox card = new VBox(10, name, status, new HBox(10, edit, delete));
        card.getStyleClass().add("event-card");
        card.setPrefWidth(250);

        return card;
    }

    private void renderCards() {
        cardContainer.getChildren().clear();
        for (Registration r : list)
            cardContainer.getChildren().add(createCard(r));
    }

    private void alert(String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setContentText(msg);
        a.showAndWait();
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/com/example/psy/Event/events.fxml");
    }
}

package Controllers.Event;

import Entities.Registration;
import Service.RegistrationService;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import util.SceneManager;

import java.sql.SQLException;
import java.util.UUID;

public class RegistrationController {

    @FXML private TextField searchField;
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
        statusBox.getItems().addAll("registered", "attended", "cancelled");
        statusBox.setValue("registered");
        searchField.textProperty().addListener((obs, oldV, newV) -> filter());

    }

    public void setEventId(int id) {
        this.eventId = id;
        eventInfoLabel.setText("Participants of Event ID: " + id);
        loadParticipants();
    }

    // ================= LOAD =================
    private void loadParticipants() {
        try {
            list.setAll(service.listByEvent(eventId));
            filtered.setAll(list);
            renderCards();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADD / UPDATE =================
    @FXML
    private void handleRegister() {

        try {
            if (nameField.getText().isEmpty()) {
                alert("Enter participant name");
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

    // ================= CARD =================
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

        Button qrBtn = new Button("QR");
        qrBtn.getStyleClass().add("btn-secondary");
        qrBtn.setOnAction(e -> showQRPopup(r.getQrCode()));

        HBox btnRow = new HBox(10, edit, delete, qrBtn);
        btnRow.setAlignment(javafx.geometry.Pos.CENTER);

        VBox card = new VBox(16, name, status, btnRow);
        card.getStyleClass().add("event-card");

        card.setPrefWidth(380);   // largeur plus grande
        card.setMinHeight(150);   // hauteur plus grande




        return card;
    }

    private void renderCards() {
        cardContainer.getChildren().clear();
        for (Registration r : filtered)

            cardContainer.getChildren().add(createCard(r));
    }
    private ObservableList<Registration> filtered = FXCollections.observableArrayList();


    // ================= FILTER BY NAME =================
    private void filter() {
        String q = searchField.getText().toLowerCase().trim();

        if (q.isEmpty()) {
            filtered.setAll(list);
        } else {
            filtered.setAll(
                    list.stream()
                            .filter(r -> r.getParticipantName().toLowerCase().contains(q))
                            .toList()
            );
        }

        renderCards();
    }


    // ================= QR POPUP =================
    private void showQRPopup(String text) {

        Label title = new Label("QR Code");
        title.setStyle("-fx-font-size:18; -fx-font-weight:bold;");

        TextArea qrText = new TextArea(text);
        qrText.setEditable(false);
        qrText.setWrapText(true);

        Button close = new Button("Close");
        close.getStyleClass().add("btn-primary");

        VBox box = new VBox(15, title, qrText, close);
        box.setAlignment(javafx.geometry.Pos.CENTER);
        box.setStyle("-fx-padding:20;");

        Stage stage = new Stage();
        stage.setTitle("QR Code");

        close.setOnAction(e -> stage.close());

        stage.setScene(new Scene(box, 320, 220));
        stage.show();
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

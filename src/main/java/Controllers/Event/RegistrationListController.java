package Controllers.Event;

import Entities.Registration;
import Service.RegistrationService;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import util.SceneManager;
import util.Session;
import Entities.User;
import Entities.Event;

import java.net.URL;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class RegistrationListController implements Initializable {

    @FXML private TextField searchField;
    @FXML private Label titleLabel;
    @FXML private FlowPane cardContainer;
    @FXML private Label totalLabel;
    @FXML private Label emptyListLabel;

    @FXML private Label totalCountLabel;
    @FXML private Label registeredCountLabel;
    @FXML private Label attendedCountLabel;
    @FXML private Label cancelledCountLabel;

    @FXML private ComboBox<String> statusFilter;
    @FXML private ComboBox<String> eventFilter;

    private final RegistrationService service = new RegistrationService();
    private final Service.EventService eventService = new Service.EventService();

    private ObservableList<Registration> registrations = FXCollections.observableArrayList();
    private ObservableList<Registration> filteredRegistrations = FXCollections.observableArrayList();
    private Map<Integer, String> eventMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        initFilters();
        loadRegistrations();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        statusFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        eventFilter.valueProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void initFilters() {
        statusFilter.getItems().setAll("All Statuses", "registered", "attended", "cancelled");
        statusFilter.setValue("All Statuses");

        try {
            Session session = util.Session.getInstance();
            List<Entities.Event> events;
            if (session.getUser().getRole().equals("therapist")) {
                events = eventService.listByOrganizer(session.getUser().getId());
            } else {
                events = eventService.list();
            }

            eventFilter.getItems().add("All Events");
            for (Entities.Event e : events) {
                String label = e.getIdEvent() + " - " + e.getTitle();
                eventFilter.getItems().add(label);
                eventMap.put(e.getIdEvent(), e.getTitle());
            }
            eventFilter.setValue("All Events");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ================= LOAD =================
    @FXML
    public void loadRegistrations() {
        try {
            Session session = util.Session.getInstance();
            String role = session.getUser().getRole().toLowerCase();
            
            if ("therapist".equals(role)) {
                registrations.setAll(service.listByTherapist(session.getUser().getId()));
            } else if ("patient".equals(role)) {
                String patientName = session.getUser().getFirstName();
                registrations.setAll(
                    service.list().stream()
                        .filter(r -> r.getParticipantName().equalsIgnoreCase(patientName))
                        .collect(Collectors.toList())
                );
            } else {
                // Admin or other roles
                registrations.setAll(service.list());
            }
            applyFilters();
            updateStats();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStats() {
        long total = registrations.size();
        long reg = registrations.stream().filter(r -> "registered".equals(r.getStatus())).count();
        long att = registrations.stream().filter(r -> "attended".equals(r.getStatus())).count();
        long can = registrations.stream().filter(r -> "cancelled".equals(r.getStatus())).count();

        if (totalCountLabel != null) totalCountLabel.setText(String.valueOf(total));
        if (registeredCountLabel != null) registeredCountLabel.setText(String.valueOf(reg));
        if (attendedCountLabel != null) attendedCountLabel.setText(String.valueOf(att));
        if (cancelledCountLabel != null) cancelledCountLabel.setText(String.valueOf(can));
    }

    // ================= CARD =================
    private VBox createCard(Registration r) {
        // Name Header
        Label name = new Label(r.getParticipantName());
        name.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2d3748;");

        // Icons & Labels (Email, Phone, Event)
        Label email = new Label("✉ " + (r.getParticipantEmail() != null ? r.getParticipantEmail() : "No email"));
        email.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");

        Label phone = new Label("📞 " + (r.getParticipantPhone() != null ? r.getParticipantPhone() : "No phone"));
        phone.setStyle("-fx-text-fill: #4a5568; -fx-font-size: 13px;");

        String eventTitle = eventMap.getOrDefault(r.getEventId(), "Event #" + r.getEventId());
        Label eventLabel = new Label("📅 " + eventTitle);
        eventLabel.setStyle("-fx-text-fill: #795548; -fx-font-weight: bold; -fx-font-size: 13px;");

        // Status Badge
        Label status = new Label(r.getStatus().toUpperCase());
        status.getStyleClass().add(getStatusBadgeClass(r.getStatus()));

        // Notes Area (Boxed like screenshot)
        Label notes = new Label(r.getParticipantNotes() != null && !r.getParticipantNotes().isEmpty() ? r.getParticipantNotes() : "No notes provided.");
        notes.setWrapText(true);
        notes.setMaxWidth(380);
        notes.setMinHeight(50);
        notes.setStyle("-fx-background-color: #f7fafc; -fx-padding: 10; -fx-background-radius: 8; -fx-text-fill: #718096; -fx-font-size: 12px;");

        // ===== ACTIONS =====
        Button checkInBtn = new Button("Check-in");
        checkInBtn.getStyleClass().add("btn-checkin");
        checkInBtn.setPrefWidth(100);
        checkInBtn.setOnAction(e -> handleQuickStatus(r, "attended"));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-cancel-red");
        cancelBtn.setPrefWidth(90);
        cancelBtn.setOnAction(e -> handleQuickStatus(r, "cancelled"));

        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-edit-blue");
        editBtn.setPrefWidth(80);
        editBtn.setOnAction(e -> showEditDialog(r));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("btn-delete-pink");
        deleteBtn.setPrefWidth(80);
        deleteBtn.setOnAction(e -> handleDelete(r));

        // Visibility based on roles (Patient can only see, not act)
        String userRole = Session.getInstance().getUser().getRole();
        boolean canManage = "admin".equals(userRole) || "therapist".equals(userRole);
        
        checkInBtn.setVisible(canManage && !"attended".equals(r.getStatus()));
        checkInBtn.setManaged(canManage && !"attended".equals(r.getStatus()));
        
        cancelBtn.setVisible(canManage && !"cancelled".equals(r.getStatus()));
        cancelBtn.setManaged(canManage && !"cancelled".equals(r.getStatus()));
        
        editBtn.setVisible("admin".equals(userRole));
        editBtn.setManaged("admin".equals(userRole));
        
        deleteBtn.setVisible("admin".equals(userRole));
        deleteBtn.setManaged("admin".equals(userRole));

        HBox actionRow = new HBox(12, checkInBtn, cancelBtn, editBtn, deleteBtn);
        actionRow.setPadding(new Insets(10, 0, 0, 0));

        VBox card = new VBox(10, name, email, phone, eventLabel, status, notes, actionRow);
        card.setPrefWidth(420);
        card.setStyle("-fx-background-color: white; -fx-padding: 25; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 15, 0, 0, 5);");

        return card;
    }

    private String getStatusBadgeClass(String s) {
        switch (s) {
            case "registered": return "badge-registered";
            case "cancelled": return "badge-cancelled";
            case "attended": return "badge-attended";
            default: return "";
        }
    }

    private void handleQuickStatus(Registration r, String newStatus) {
        try {
            r.setStatus(newStatus);
            service.update(r);
            loadRegistrations();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleDelete(Registration r) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Delete registration for " + r.getParticipantName() + "?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    service.delete(r.getIdRegistration());
                    loadRegistrations();
                } catch (SQLException e) { e.printStackTrace(); }
            }
        });
    }

    // ================= EDIT DIALOG =================
    private void showEditDialog(Registration r) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Registration Details");
        dialog.setHeaderText("Update participant information and status");

        // UI Components
        TextField nameField = new TextField(r.getParticipantName());
        TextField emailField = new TextField(r.getParticipantEmail());
        TextField phoneField = new TextField(r.getParticipantPhone());
        TextArea notesArea = new TextArea(r.getParticipantNotes());
        notesArea.setPrefRowCount(3);
        notesArea.setWrapText(true);

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("registered", "cancelled", "attended");
        statusBox.setValue(r.getStatus());

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold; -fx-font-size: 11px;");
        errorLabel.setVisible(false);

        GridPane grid = new GridPane();
        grid.setHgap(15); grid.setVgap(15);
        grid.setPadding(new Insets(20));

        grid.add(new Label("Name:"), 0, 0); grid.add(nameField, 1, 0);
        grid.add(new Label("Email:"), 0, 1); grid.add(emailField, 1, 1);
        grid.add(new Label("Phone:"), 0, 2); grid.add(phoneField, 1, 2);
        grid.add(new Label("Status:"), 0, 3); grid.add(statusBox, 1, 3);
        grid.add(new Label("Notes:"), 0, 4); grid.add(notesArea, 1, 4);
        grid.add(errorLabel, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        final Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (util.ValidationUtil.isEmpty(nameField.getText())) {
                errorLabel.setText("Name is required."); errorLabel.setVisible(true);
                event.consume();
            }
        });

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                r.setParticipantName(nameField.getText() == null ? "" : nameField.getText().trim());
                r.setParticipantEmail(emailField.getText() == null ? "" : emailField.getText().trim());
                r.setParticipantPhone(phoneField.getText() == null ? "" : phoneField.getText().trim());
                r.setParticipantNotes(notesArea.getText() == null ? "" : notesArea.getText().trim());
                r.setStatus(statusBox.getValue());
                service.update(r);
                loadRegistrations();
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Success");
                alert.setHeaderText(null);
                alert.setContentText("Registration updated successfully!");
                alert.show();
                
            } catch (Exception ex) {
                ex.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Update Failed");
                alert.setContentText(ex.getMessage());
                alert.show();
            }
        }
    }

    // ================= RENDER =================
    private void renderCards() {
        cardContainer.getChildren().clear();
        boolean isEmpty = filteredRegistrations.isEmpty();

        if (emptyListLabel != null) {
            emptyListLabel.setVisible(isEmpty);
            emptyListLabel.setManaged(isEmpty);
        }

        for (Registration r : filteredRegistrations) {
            cardContainer.getChildren().add(createCard(r));
        }
    }

    private void applyFilters() {
        String query = searchField.getText().toLowerCase().trim();
        String status = statusFilter.getValue();
        String eventFilterVal = eventFilter.getValue();

        filteredRegistrations.setAll(
                registrations.stream()
                        .filter(r -> {
                            boolean matchesSearch = query.isEmpty() ||
                                    r.getParticipantName().toLowerCase().contains(query) ||
                                    (r.getParticipantEmail() != null && r.getParticipantEmail().toLowerCase().contains(query));
                            boolean matchesStatus = status == null || "All Statuses".equals(status) || status.equals(r.getStatus());
                            boolean matchesEvent = true;
                            if (eventFilterVal != null && !"All Events".equals(eventFilterVal)) {
                                int eventId = Integer.parseInt(eventFilterVal.split(" - ")[0]);
                                matchesEvent = (r.getEventId() == eventId);
                            }
                            return matchesSearch && matchesStatus && matchesEvent;
                        })
                        .collect(Collectors.toList())
        );
        renderCards();
    }

    @FXML
    private void handleBack() {
        SceneManager.loadPage("/com/example/psy/Event/events.fxml");
    }

    public void handleAddOrUpdate(ActionEvent actionEvent) { }
}

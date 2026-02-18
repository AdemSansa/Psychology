package Controllers.Event;


import Service.RegistrationService;
import Entities.Event;
import Service.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import util.SceneManager;
import util.Session;
import Entities.User;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class EventListController implements Initializable {
    private VBox selectedCard = null;


    @FXML private TextField searchField;
    @FXML private FlowPane cardContainer;
    @FXML private Label totalEventsLabel;
    @FXML private Button registrationsBtn;
    @FXML private Button addEventBtn;

    private final EventService eventService = new EventService();

    private ObservableList<Event> events = FXCollections.observableArrayList();
    private ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEventsFromDB();

        String role = Session.getInstance().getUser().getRole();
        boolean canManage = "therapist".equals(role) || "admin".equals(role);
        
        registrationsBtn.setVisible(canManage);
        registrationsBtn.setManaged(canManage);
        addEventBtn.setVisible(canManage);
        addEventBtn.setManaged(canManage);

        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        // ===== FIX SCROLL =====
        cardContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            cardContainer.setPrefHeight(newVal.doubleValue());
        });
    }

    // ================= LOAD =================
    private void loadEventsFromDB() {
        try {
            Session session = Session.getInstance();
            if ("therapist".equals(session.getUser().getRole())) {
                events.setAll(eventService.listByOrganizer(session.getUser().getId()));
            } else {
                // Admin or Guest/Patient (Guests see all published events usually)
                events.setAll(eventService.list());
            }
            filteredEvents.setAll(events);
            renderCards();
            updateTotalLabel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CARD =================
    private VBox createEventCard(Event event) {
        // ===== COUNTDOWN =====
        Label countdownLabel = new Label(getCountdown(event));

// 🔥 COLOR AUTO
        LocalDateTime now = LocalDateTime.now();

        if (event.getDateEnd() != null && now.isAfter(event.getDateEnd())) {
            countdownLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        }
        else if (event.getDateStart() != null && now.isAfter(event.getDateStart())) {
            countdownLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        }
        else {
            countdownLabel.setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
        }
        // Allow Admin OR Therapist to edit/delete (back office)
        String role2 = Session.getInstance().getUser().getRole();
        boolean isAdminOrTherapist = "admin".equals(role2) || "therapist".equals(role2);
        
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");

        editBtn.getStyleClass().add("btn-primary");
        deleteBtn.getStyleClass().add("btn-danger");

        // ONLY Admin or Therapist can See Edit/Delete
        editBtn.setVisible(isAdminOrTherapist);
        editBtn.setManaged(isAdminOrTherapist);
        deleteBtn.setVisible(isAdminOrTherapist);
        deleteBtn.setManaged(isAdminOrTherapist);



        // ===== IMAGE =====
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(290, 170);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(290);
        imageView.setFitHeight(170);
        imageView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(290, 170);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                String imageUrl = event.getImageUrl();
                String finalUrl = imageUrl.startsWith("http") ? imageUrl : new java.io.File(imageUrl).toURI().toString();
                imageView.setImage(new Image(finalUrl, true));
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                imageContainer.getChildren().add(createImagePlaceholder());
            }
        } else {
            imageContainer.getChildren().add(createImagePlaceholder());
        }

        // ===== TITLE =====
        Label titleLabel = new Label(event.getTitle());
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(260);
        titleLabel.setStyle("""
            -fx-font-size: 18px;
            -fx-font-weight: bold;
            -fx-text-fill: #3e2c23;
        """);

        // ===== META INFO =====
        Label startLabel = new Label("📅 Start: " + formatDate(event.getDateStart()));
        Label endLabel = new Label("⏱ End: " + formatDate(event.getDateEnd()));
        int registered = RegistrationService.countByEvent(event.getIdEvent());
        int max = event.getMaxParticipants();
        int dispo = max - registered;

        Label participantsLabel = new Label("👥 Participants: " + registered + " / " + max);
        Label dispoLabel = new Label();

        if (dispo > 0) {
            dispoLabel.setText("🟢 Dispo: " + dispo);
            dispoLabel.setStyle("""
        -fx-text-fill: #2e7d32;
        -fx-font-weight: bold;
        -fx-font-size: 12px;
    """);
        } else {
            dispoLabel.setText("🔴 FULL");
            dispoLabel.setStyle("""
        -fx-text-fill: #c62828;
        -fx-font-weight: bold;
        -fx-font-size: 12px;
    """);
        }


        startLabel.setStyle("-fx-text-fill: #6f4e37; -fx-font-size: 12px;");
        endLabel.setStyle("-fx-text-fill: #6f4e37; -fx-font-size: 12px;");
        participantsLabel.setStyle("-fx-text-fill: #6f4e37; -fx-font-size: 12px;");

        HBox participantsRow = new HBox(10, participantsLabel, dispoLabel);
        participantsRow.setAlignment(Pos.CENTER_LEFT);

        VBox metaBox = new VBox(4, startLabel, endLabel, participantsRow);


        // ===== TYPE BADGE =====
        Label typeBadge = new Label(event.getType());
        typeBadge.setStyle("""
    -fx-background-color: #e3f2fd;
    -fx-text-fill: #1565c0;
    -fx-background-radius: 14;
    -fx-padding: 3 10;
""");

// ===== STATUS =====
        Label statusBadge = new Label(event.getStatus());
        statusBadge.setStyle("""
    -fx-background-color: #ede7f6;
    -fx-text-fill: #5e35b1;
    -fx-background-radius: 14;
    -fx-padding: 3 10;
""");

// 🔥 ALIGN TYPE + STATUS (IMPORTANT)
        HBox badgeRow = new HBox(8, typeBadge, statusBadge);
        badgeRow.setAlignment(Pos.CENTER_LEFT);

        // ===== DESCRIPTION =====
        Label descLabel = new Label(event.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(260);
        descLabel.setMaxHeight(80);
        descLabel.setStyle("""
            -fx-font-size: 13px;
            -fx-text-fill: #5c4a3f;
        """);

        // ===== BUTTONS =====

        editBtn.getStyleClass().add("btn-primary");
        editBtn.setOnAction(e -> handleEdit(event));
        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("btn-primary");

        // 🚫 Hide for Therapist (they manage)
        boolean isTherapist = "therapist".equals(role2);
        if (isTherapist) {
            registerBtn.setVisible(false);
            registerBtn.setManaged(false);
        }

        // 🚫 Disable if FULL
        if (dispo <= 0) {
            registerBtn.setDisable(true);
            registerBtn.setText("FULL");
        }


        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setOnAction(e -> {

            RegistrationController controller =
                    (RegistrationController) SceneManager.loadPageWithController(
                            "/com/example/psy/Event/registration_list.fxml"
                    );

            if (controller != null) {
                controller.setEventId(event.getIdEvent());   // 🔥 ENVOIE EVENT ID
            }
        });




        deleteBtn.getStyleClass().add("btn-primary");
        deleteBtn.setOnAction(e -> {
            try {
                handleDelete(event);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox btnRow = new HBox(10, registerBtn, editBtn, deleteBtn);
        btnRow.setAlignment(Pos.CENTER);


        HBox bottomRow = new HBox(btnRow);
        bottomRow.setAlignment(Pos.CENTER);



        // ===== CARD =====
        VBox card = new VBox(15); // Increased spacing
        card.setPrefWidth(350);
        card.setStyle("-fx-background-color: white; -fx-padding: 30; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5);");
        card.getStyleClass().add("event-card");

        card.getChildren().addAll(
                imageContainer,
                titleLabel,
                metaBox,
                countdownLabel,
                badgeRow,   // 🔥 UTILISE badgeRow PAS typeBadge
                descLabel,
                bottomRow
        );

        card.setOnMouseClicked(e -> {

            // retirer ancienne sélection
            if (selectedCard != null) {
                selectedCard.getStyleClass().remove("event-card-selected");
            }

            // sélectionner nouvelle carte
            selectedCard = card;
            card.getStyleClass().add("event-card-selected");

        });




        return card;
    }

    private StackPane createImagePlaceholder() {
        Rectangle bg = new Rectangle(290, 170);
        bg.setFill(Color.LIGHTGRAY);
        return new StackPane(bg, new Label("No Image"));
    }

    private void renderCards() {
        cardContainer.getChildren().clear();
        for (Event e : filteredEvents) {
            cardContainer.getChildren().add(createEventCard(e));
        }
    }

    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) filteredEvents.setAll(events);
        else filteredEvents.setAll(
                events.stream()
                        .filter(e -> e.getTitle().toLowerCase().contains(query)
                                || e.getDescription().toLowerCase().contains(query))
                        .collect(Collectors.toList())
        );
        renderCards();
        updateTotalLabel();
    }

    // ================= NAVIGATION =================
    @FXML
    private void handleBack() {
        SceneManager.loadPage("/com/example/psy/intro/dashboard.fxml");
    }

    @FXML
    private void handleAddEvent() {
        SceneManager.loadPage("/com/example/psy/Event/eventAdd.fxml");
    }

    @FXML
    private void handleViewRegistrations() {
        SceneManager.loadPage("/com/example/psy/Event/registration.fxml");
    }

    private void handleEdit(Event event) {
        EventEditController controller =
                (EventEditController) SceneManager.switchSceneWithController("/com/example/psy/Event/eventEdit.fxml");
        if (controller != null) {
            controller.setEvent(event);
        }
    }

    private void handleDelete(Event event) throws SQLException {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Delete this event?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            eventService.delete(event.getIdEvent());
            loadEventsFromDB();
        }
    }

    private void updateTotalLabel() {
        totalEventsLabel.setText("Total events: " + filteredEvents.size());
    }

    private String formatDate(LocalDateTime date) {
        return date != null ? date.format(DATE_FORMATTER) : "TBA";
    }
    //-------getcountdown---------
    private String getCountdown(Event event) {

        if (event.getDateStart() == null) return "";

        LocalDateTime now = LocalDateTime.now();

        if (event.getDateEnd() != null && now.isAfter(event.getDateEnd()))
            return "Finished";

        if (now.isAfter(event.getDateStart()))
            return "Started";

        long minutes = java.time.Duration.between(now, event.getDateStart()).toMinutes();

        long days = minutes / (60 * 24);
        long hours = (minutes % (60 * 24)) / 60;
        long mins = minutes % 60;

        return "⏳ " + days + "d " + hours + "h " + mins + "m";

    }



}

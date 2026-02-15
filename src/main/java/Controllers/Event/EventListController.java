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

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class EventListController implements Initializable {

    @FXML private TextField searchField;
    @FXML private FlowPane cardContainer;
    @FXML private Label totalEventsLabel;

    private final EventService eventService = new EventService();

    private ObservableList<Event> events = FXCollections.observableArrayList();
    private ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEventsFromDB();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        // ===== FIX SCROLL =====
        cardContainer.heightProperty().addListener((obs, oldVal, newVal) -> {
            cardContainer.setPrefHeight(newVal.doubleValue());
        });
    }

    // ================= LOAD =================
    private void loadEventsFromDB() {
        try {
            events.setAll(eventService.list());
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


        // ===== IMAGE =====
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(300, 170);

        ImageView imageView = new ImageView();
        imageView.setFitWidth(300);
        imageView.setFitHeight(170);
        imageView.setPreserveRatio(false);

        Rectangle clip = new Rectangle(300, 170);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                imageView.setImage(new Image(event.getImageUrl(), true));
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
        Label dispoLabel = new Label("🟢 Dispo: " + dispo);

        participantsLabel.setStyle("-fx-text-fill: #6f4e37; -fx-font-size: 12px;");
        dispoLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");

// 🔴 FULL si plus de places
        if (dispo <= 0) {
            dispoLabel.setText("🔴 FULL");
            dispoLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
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
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("btn-primary");
        editBtn.setOnAction(e -> handleEdit(event));
        Button registerBtn = new Button("Register");
        registerBtn.getStyleClass().add("btn-primary");

// 🚫 Désactiver si FULL
        if (dispo <= 0) {
            registerBtn.setDisable(true);
            registerBtn.setText("FULL");
        }

        registerBtn.setOnAction(e -> {
            RegistrationController controller =
                    (RegistrationController) SceneManager.switchSceneWithController(
                            "/com/example/psy/Event/registration.fxml"
                    );

            if (controller != null) {
                controller.setEventId(event.getIdEvent());
            }

        });

        registerBtn.getStyleClass().add("btn-primary");
        registerBtn.setOnAction(e -> {

            RegistrationController controller =
                    (RegistrationController) SceneManager.switchSceneWithController(
                            "/com/example/psy/Event/registration.fxml"
                    );

            if (controller != null) {
                controller.setEventId(event.getIdEvent());   // 🔥 ENVOIE EVENT ID
            }
        });



        Button deleteBtn = new Button("Delete");
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
        VBox card = new VBox(12);
        card.setPrefWidth(300);
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




        return card;
    }

    private StackPane createImagePlaceholder() {
        Rectangle bg = new Rectangle(300, 170);
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
        SceneManager.switchScene("/com/example/psy/intro/Home.fxml");
    }

    @FXML
    private void handleAddEvent() {
        SceneManager.switchScene("/com/example/psy/Event/eventAdd.fxml");
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

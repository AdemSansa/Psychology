package Controllers.Event;


import Entities.Event;
import Service.EventService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import util.SceneManager;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EventListController implements Initializable {

    // ===== FXML injected components =====
    @FXML private TextField searchField;
    @FXML private FlowPane cardContainer;
    @FXML private Label totalEventsLabel;
    private final EventService eventService = new EventService();

    // ===== Data =====
    private ObservableList<Event> events = FXCollections.observableArrayList();
    private ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

    // ===== Date formatter =====
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm");
    private void refreshEvents() {
        loadEventsFromDB();
    }
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadEventsFromDB();
        // 2. Initially filtered = all events
        filteredEvents.setAll(events);

        // 3. Render cards
        renderCards();

        // 4. Update total label
        updateTotalLabel();

        // 5. Add live search listener
        searchField.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
    }
    private void loadEventsFromDB() {
        try {
            events.setAll(eventService.list());  // fetch from DB
            filteredEvents.setAll(events);
            renderCards();
            updateTotalLabel();
            System.out.println("Loaded " + events.size() + " events from DB.");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to load events.");
        }
    }
    // ===== CARD CREATION =====
    private StackPane createImagePlaceholder() {
        Rectangle bg = new Rectangle(260, 140);
        bg.setFill(Color.web("#ac9885"));
        bg.setArcWidth(16);
        bg.setArcHeight(16);

        Label icon = new Label("No Image");
        icon.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        return new StackPane(bg, icon);
    }
    private VBox createEventCard(Event event) {
        // ----- Image placeholder (accent beige) -----
        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(260, 140);
        imageContainer.setMaxSize(260, 140);
        imageContainer.setStyle("-fx-background-radius: 12; -fx-overflow: hidden;");

        ImageView imageView = new ImageView();
        imageView.setFitWidth(260);
        imageView.setFitHeight(140);
        imageView.setPreserveRatio(false);

// Rounded clip
        Rectangle clip = new Rectangle(260, 140);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);

        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            try {
                Image img = new Image(event.getImageUrl(), true);
                imageView.setImage(img);
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                imageContainer.getChildren().add(createImagePlaceholder());
            }
        } else {
            imageContainer.getChildren().add(createImagePlaceholder());
        }



        // ----- Title -----
        Label titleLabel = new Label(event.getTitle());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #3f4f4f;");

        // ----- Date & location (metadata) -----
        Label dateLabel = new Label(formatDate(event.getDateStart()));
        dateLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7a8a8a;");

        Label locationLabel = new Label(event.getLocation());
        locationLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #7a8a8a;");

        HBox metaRow = new HBox(8, dateLabel, new Label("•"), locationLabel);
        metaRow.setAlignment(Pos.CENTER_LEFT);
        metaRow.setStyle("-fx-font-size: 13px; -fx-text-fill: #7a8a8a;");

        // ----- Type badge (soft green chip) -----
        Label typeBadge = new Label(event.getType());
        typeBadge.setStyle(
                "-fx-background-color: derive(#7b9e7f, 80%);" +
                        "-fx-text-fill: #3f4f4f;" +
                        "-fx-background-radius: 14;" +
                        "-fx-padding: 4 12 4 12;" +
                        "-fx-font-size: 12px;"
        );

        // ----- Description (truncated) -----
        Label descLabel = new Label(event.getDescription());
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(240);
        descLabel.setMaxHeight(60);
        descLabel.setStyle(
                "-fx-font-size: 13px;" +
                        "-fx-text-fill: #3f4f4f;" +
                        "-fx-line-spacing: 2;"
        );

        // ----- Status badge (uses existing CSS classes) -----
        Label statusBadge = new Label(event.getStatus());
        statusBadge.getStyleClass().addAll("status-badge");
        switch (event.getStatus().toLowerCase()) {
            case "published":
                statusBadge.getStyleClass().add("status-scheduled"); // green
                break;
            case "cancelled":
                statusBadge.getStyleClass().add("status-cancelled"); // soft red
                break;
            case "draft":
            default:
                statusBadge.getStyleClass().add("status-full");      // beige
                break;
        }

        // ----- Action buttons (link-button style) -----
        Button editBtn = new Button("Edit");
        editBtn.getStyleClass().add("link-button");
        editBtn.setOnAction(e -> handleEdit(event));

        Button deleteBtn = new Button("Delete");
        deleteBtn.getStyleClass().add("link-button");
        deleteBtn.setOnAction(e -> {
            try {
                handleDelete(event);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });

        HBox actions = new HBox(15, editBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        // ----- Row: status badge (left) + actions (right) -----
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox statusActionRow = new HBox(10, statusBadge, spacer, actions);
        statusActionRow.setAlignment(Pos.CENTER_LEFT);

        // ----- Assemble card -----
        VBox card = new VBox(12);
        card.getStyleClass().add("login-card");   // white background, shadow
        card.setStyle(
                "-fx-padding: 16;" +
                        "-fx-background-radius: 18;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 12, 0, 0, 4);"
        );
        card.setPrefWidth(280);
        card.setMaxWidth(280);

        card.getChildren().addAll(
                imageContainer,
                titleLabel,
                metaRow,
                typeBadge,
                descLabel,
                statusActionRow
        );

        return card;
    }

    // ===== RENDER CARDS =====
    private void renderCards() {
        cardContainer.getChildren().clear();
        for (Event event : filteredEvents) {
            cardContainer.getChildren().add(createEventCard(event));
        }
    }

    // ===== SEARCH =====
    @FXML
    private void handleSearch() {
        String query = searchField.getText().toLowerCase().trim();
        if (query.isEmpty()) {
            filteredEvents.setAll(events);
        } else {
            filteredEvents.setAll(
                    events.stream()
                            .filter(e -> e.getTitle().toLowerCase().contains(query) ||
                                    e.getDescription().toLowerCase().contains(query))
                            .collect(Collectors.toList())
            );
        }
        renderCards();
        updateTotalLabel();
    }

    // ===== ADD EVENT =====
    @FXML
    private void handleAddEvent() {


            SceneManager.switchScene("/com/example/psy/Event/eventAdd.fxml");
        refreshEvents();

    }

    // ===== EDIT EVENT =====
    private void handleEdit(Event event) {


             EventEditController controller = SceneManager.switchSceneWithController("/com/example/psy/Event/eventEdit.fxml");
             controller.setEvent(event);


            // 🔄 Refresh after editing
            refreshEvents();

    }

    // ===== DELETE EVENT =====
    private void handleDelete(Event event) throws SQLException {

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Confirm Deletion");
        confirmation.setHeaderText("Delete Event");
        confirmation.setContentText("Are you sure you want to delete this event?");

        Optional<ButtonType> result = confirmation.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {

            // Remove from UI lists
            events.remove(event);
            filteredEvents.remove(event);

            // Remove from database
            eventService.delete(event.getIdEvent());

            // Refresh UI
            renderCards();
            updateTotalLabel();

            showAlert(Alert.AlertType.INFORMATION,
                    "Deleted",
                    "Event deleted successfully.");
        }
    }

    // ===== HELPER: format date =====
    private String formatDate(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "TBA";
    }

    // ===== UPDATE TOTAL LABEL =====
    private void updateTotalLabel() {
        totalEventsLabel.setText("Total events: " + filteredEvents.size());
    }

    // ===== SAMPLE DATA =====


    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
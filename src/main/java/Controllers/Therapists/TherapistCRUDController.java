package Controllers.Therapists;

import Entities.Specialization;
import Entities.Therapistis;
import Entities.User;
import Service.TherapistService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;
import java.util.Scanner;
import util.DiplomaValidator;
import util.PasswordUtil;
import util.SceneManager;
import util.Session;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TherapistCRUDController implements Initializable {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label totalTherapistsLabel;

    // ── Search & Filter ─────────────────────────────────────────
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> specialityFilterBox;
    @FXML
    private ComboBox<String> consultTypeFilterBox;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private TextField locationSearchField;
    @FXML
    private Label searchResultLabel;
    @FXML
    private Label emptyStateLabel;
    @FXML
    private Button resetFilterBtn;

    // ── Profile Detail Modal ────────────────────────────────────
    @FXML
    private StackPane profileModalOverlay;
    @FXML
    private Label profileInitialsLabel;
    @FXML
    private Label profileNameLabel;
    @FXML
    private Label profileSpecLabel;
    @FXML
    private Label profileStatusBadge;
    @FXML
    private Label profileConsultTypeLabel;
    @FXML
    private Label profileMemberSinceLabel;
    @FXML
    private Label profileEmailLabel;
    @FXML
    private Label profilePhoneLabel;
    @FXML
    private Label profileSpecFullLabel;
    @FXML
    private Label profileDescLabel;
    @FXML
    private Button rdvButton;
    @FXML
    private Button profileDiplomaBtn;
    @FXML
    private ImageView profilePhotoView; // NEW: Profile photo in modal

    // ── Add/Edit Modal ──────────────────────────────────────────
    @FXML
    private StackPane modalOverlay;
    @FXML
    private Label modalTitle;
    @FXML
    private TextField firstNameField;
    @FXML
    private TextField lastNameField;
    @FXML
    private TextField emailField;
    @FXML
    private TextField phoneField;
    @FXML
    private ComboBox<String> specializationField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private ComboBox<String> consultationTypeBox;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField photoUrlField; // NEW: photo URL input
    @FXML
    private ImageView photoPreview; // NEW: circular preview thumbnail
    @FXML
    private Label photoPreviewInitials; // NEW: "?" placeholder

    // ── Diploma Upload ──────────────────────────────────────────
    @FXML
    private Label diplomaFileLabel;
    @FXML
    private Label diplomaStatusLabel;

    @FXML
    private TextField latitudeField;
    @FXML
    private TextField longitudeField;
    @FXML
    private StackPane mapContainer;

    private WebView mapView;

    private File selectedDiplomaFile = null;
    private boolean diplomaValid = false;

    @FXML
    private Button docteur;
    @FXML
    private Button ajoutdocteur;
    @FXML
    private Button dispo;

    private TherapistService service;
    private ObservableList<Therapistis> therapistList;
    private Therapistis currentTherapist = null;
    // Therapist currently shown in the profile modal (for RDV navigation)
    private Therapistis selectedProfileTherapist = null;

    private double searchLat = 36.8065; // Default: Tunis
    private double searchLon = 10.1815;
    private static final double MAX_DISTANCE_KM = 200.0; // Distance threshold for location searches

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = Session.getInstance().getUser();
        service = new TherapistService();
        therapistList = FXCollections.observableArrayList();
        consultationTypeBox.setItems(FXCollections.observableArrayList("ONLINE", "IN_PERSON", "BOTH"));

        // Populate specialization field in Add/Edit modal
        ObservableList<String> specOptions = FXCollections.observableArrayList();
        for (Specialization s : Specialization.values()) {
            specOptions.add(s.getDisplayName());
        }
        specializationField.setItems(specOptions);

        loadSpecialityFilter();
        consultTypeFilterBox.setItems(FXCollections.observableArrayList(
                "All modes", "🌐 Online", "🏥 In person", "🌐🏥 Both"));
        consultTypeFilterBox.getSelectionModel().selectFirst();

        sortComboBox.setItems(FXCollections.observableArrayList(
                "Default", "📍 Closest", "🔤 Name (A-Z)"));
        sortComboBox.getSelectionModel().selectFirst();

        // Listeners for auto-filtering
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTherapists());
        specialityFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTherapists());
        consultTypeFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTherapists());
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTherapists());

        loadTherapists();

        // Live photo preview in the add/edit form
        photoUrlField.textProperty().addListener((obs, oldVal, newVal) -> {
            Image img = loadImageSafe(newVal.trim());
            if (img != null) {
                photoPreview.setImage(img);
                photoPreview.setVisible(true);
                photoPreviewInitials.setVisible(false);
            } else {
                photoPreview.setVisible(false);
                photoPreviewInitials.setVisible(true);
            }
        });

        updateVisibility(user.getRole());
    }

    // ─── Speciality filter list ───────────────────────────────────────────────
    private void loadSpecialityFilter() {
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("All specialties");
        for (Specialization s : Specialization.values()) {
            items.add(s.getDisplayName());
        }
        specialityFilterBox.setItems(items);
        specialityFilterBox.getSelectionModel().selectFirst();
    }

    // ─── Load all therapists from DB ─────────────────────────────────────────
    private void loadTherapists() {
        if (cardsContainer == null)
            return;
        cardsContainer.getChildren().clear();
        try {
            List<Therapistis> list = service.list();
            therapistList.setAll(list);
            if (totalTherapistsLabel != null)
                totalTherapistsLabel.setText(list.size() + " therapist(s) registered");
            renderCards(list);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error", "Unable to load therapists: " + e.getMessage());
        }
    }

    // ─── Client-side filter ──────────────────────────────────────────────────
    private void filterTherapists() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedSpec = specialityFilterBox.getValue();
        String selectedMode = consultTypeFilterBox.getValue();
        String selectedSort = sortComboBox.getValue();

        boolean allSpecs = selectedSpec == null || selectedSpec.equals("All specialties");
        boolean allModes = selectedMode == null || selectedMode.equals("All modes");

        List<Therapistis> filtered = therapistList.stream()
                .filter(t -> {
                    // Name, Description, and Specialization search
                    boolean keywordMatch = keyword.isEmpty()
                            || (t.getFirstName() != null && t.getFirstName().toLowerCase().contains(keyword))
                            || (t.getLastName() != null && t.getLastName().toLowerCase().contains(keyword))
                            || ((t.getFirstName() + " " + t.getLastName()).toLowerCase().contains(keyword))
                            || (t.getDescription() != null && t.getDescription().toLowerCase().contains(keyword))
                            || (t.getSpecialization() != null && t.getSpecialization().toLowerCase().contains(keyword));

                    // Speciality filter (Exact match)
                    boolean specMatch = allSpecs
                            || (t.getSpecialization() != null && t.getSpecialization().equalsIgnoreCase(selectedSpec));
                    // Consultation type filter
                    boolean modeMatch = allModes || matchConsultType(t.getConsultationType(), selectedMode);

                    // Distance Filter (Apply only if location search is active)
                    boolean distanceMatch = true;
                    String locKeyword = locationSearchField.getText();
                    if (locKeyword != null && !locKeyword.trim().isEmpty()) {
                        // If searching by location, exclude those without coords or too far
                        if (t.getLatitude() == 0 && t.getLongitude() == 0) {
                            distanceMatch = false;
                        } else {
                            double dist = calculateDistance(searchLat, searchLon, t.getLatitude(), t.getLongitude());
                            distanceMatch = dist <= MAX_DISTANCE_KM;
                        }
                    }

                    return keywordMatch && specMatch && modeMatch && distanceMatch;
                })
                .collect(Collectors.toList());

        // Apply Sorting
        if (selectedSort != null) {
            if (selectedSort.contains("Closest")) {
                filtered.sort((t1, t2) -> {
                    double d1 = calculateDistance(searchLat, searchLon, t1.getLatitude(), t1.getLongitude());
                    double d2 = calculateDistance(searchLat, searchLon, t2.getLatitude(), t2.getLongitude());

                    // Push those without coords to the end
                    if (t1.getLatitude() == 0 && t1.getLongitude() == 0)
                        return 1;
                    if (t2.getLatitude() == 0 && t2.getLongitude() == 0)
                        return -1;

                    return Double.compare(d1, d2);
                });
            } else if (selectedSort.contains("Name (A-Z)")) {
                filtered.sort((t1, t2) -> (t1.getLastName() + t1.getFirstName())
                        .compareToIgnoreCase(t2.getLastName() + t2.getFirstName()));
            }
        }

        renderCards(filtered);

        boolean activeFilters = !keyword.isEmpty() || !allSpecs || !allModes;
        String locKeyword = locationSearchField.getText();
        boolean hasLocation = locKeyword != null && !locKeyword.trim().isEmpty();

        String resultText = "";
        if (activeFilters || hasLocation) {
            resultText = filtered.size() + " result(s)";
            if (hasLocation) {
                resultText += " (200km radius)";
            }
        }
        searchResultLabel.setText(resultText);
    }

    @FXML
    void handleLocationSearch(ActionEvent event) {
        String locName = locationSearchField.getText();
        if (locName == null || locName.trim().isEmpty()) {
            searchLat = 36.8065; // Reset to Tunis
            searchLon = 10.1815;
            filterTherapists();
            return;
        }

        // Run in a separate thread to avoid freezing UI
        new Thread(() -> {
            try {
                double[] coords = fetchCoordinates(locName.trim());
                if (coords != null) {
                    searchLat = coords[0];
                    searchLon = coords[1];
                    javafx.application.Platform.runLater(() -> {
                        if (!"📍 Closest".equals(sortComboBox.getValue())) {
                            sortComboBox.setValue("📍 Closest");
                        }
                        filterTherapists();
                    });
                } else {
                    javafx.application.Platform
                            .runLater(() -> showAlert("Location", "Unable to find location: " + locName));
                }
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform
                        .runLater(() -> showAlert("Error", "Error while searching for location."));
            }
        }).start();
    }

    @FXML
    void handleModalLocationLookup(ActionEvent event) {
        String address = descriptionArea.getText();
        if (address == null || address.trim().isEmpty()) {
            showAlert("Location", "Please enter an address in the description to locate the therapist.");
            return;
        }

        new Thread(() -> {
            try {
                double[] coords = fetchCoordinates(address.trim());
                if (coords != null) {
                    javafx.application.Platform.runLater(() -> {
                        latitudeField.setText(String.valueOf(coords[0]));
                        longitudeField.setText(String.valueOf(coords[1]));
                    });
                } else {
                    javafx.application.Platform
                            .runLater(
                                    () -> showAlert("Location", "Place not found in description: " + address));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private double[] fetchCoordinates(String locationName) throws Exception {
        // Try exact match first
        double[] result = performGeocoding(locationName);

        // If fail, try with ", Tunisie" as fallback since the app is mainly for Tunisia
        if (result == null && !locationName.toLowerCase().contains("tunisi")) {
            System.out.println("Location search for '" + locationName + "' failed, trying fallback...");
            result = performGeocoding(locationName + ", Tunisie");
        }

        return result;
    }

    private double[] performGeocoding(String query) throws Exception {
        String urlStr = "https://nominatim.openstreetmap.org/search?q="
                + URLEncoder.encode(query, StandardCharsets.UTF_8)
                + "&format=json&limit=1";

        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        // Descriptive User-Agent is required by Nominatim usage policy
        conn.setRequestProperty("User-Agent", "PsychologyApp/1.0 (Contact: psychiatric-app@esi.tn)");
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);

        if (conn.getResponseCode() == 200) {
            try (InputStream is = conn.getInputStream()) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(is);
                if (root.isArray() && root.size() > 0) {
                    double lat = root.get(0).get("lat").asDouble();
                    double lon = root.get(0).get("lon").asDouble();
                    return new double[] { lat, lon };
                }
            }
        }
        return null;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        if (lat2 == 0 && lon2 == 0)
            return Double.MAX_VALUE;

        double R = 6371; // Earth radius in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private boolean matchConsultType(String therapistType, String selectedLabel) {
        if (therapistType == null)
            return false;
        return switch (selectedLabel) {
            case "🌐 Online" -> therapistType.equalsIgnoreCase("ONLINE");
            case "🏥 In person" -> therapistType.equalsIgnoreCase("IN_PERSON");
            case "🌐🏥 Both" -> therapistType.equalsIgnoreCase("BOTH");
            default -> true;
        };
    }

    // ─── Safe image loader (catches bad URLs gracefully) ──────────────────────
    private Image loadImageSafe(String url) {
        if (url == null || url.isBlank())
            return null;
        try {
            Image img = new Image(url, true); // background loading
            // Check if it loaded (synchronous check via progress)
            img.errorProperty().addListener((obs, wasErr, isErr) -> {
            }); // suppress
            if (img.isError())
                return null;
            return img;
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Render cards ────────────────────────────────────────────────────────
    private void renderCards(List<Therapistis> list) {
        cardsContainer.getChildren().clear();
        boolean isEmpty = list.isEmpty();
        if (emptyStateLabel != null) {
            emptyStateLabel.setVisible(isEmpty);
            emptyStateLabel.setManaged(isEmpty);
        }

        User user = Session.getInstance().getUser();
        for (Therapistis t : list) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/com/example/psy/Therapist/therapist_card.fxml"));
                Node card = loader.load();

                Label nameLabel = (Label) card.lookup("#nameLabel");
                Label roleLabel = (Label) card.lookup("#roleLabel");
                Label initialsLabel = (Label) card.lookup("#initialsLabel");
                Label emailLabel = (Label) card.lookup("#emailLabel");
                Label phoneLabel = (Label) card.lookup("#phoneLabel");
                Label addressLabel = (Label) card.lookup("#addressLabel");
                Label specializationLabel = (Label) card.lookup("#specializationLabel");
                Button editBtn = (Button) card.lookup("#editButton");
                Button deleteBtn = (Button) card.lookup("#deleteButton");

                if (nameLabel != null)
                    nameLabel.setText(t.getFirstName() + " " + t.getLastName());
                if (roleLabel != null)
                    roleLabel.setText(t.getConsultationType());

                if (initialsLabel != null) {
                    String initials = "";
                    if (t.getFirstName() != null && !t.getFirstName().isEmpty())
                        initials += t.getFirstName().charAt(0);
                    if (t.getLastName() != null && !t.getLastName().isEmpty())
                        initials += t.getLastName().charAt(0);
                    initialsLabel.setText(initials.toUpperCase());
                }

                // Photo: show photo over initials if URL is valid
                ImageView photoView = (ImageView) card.lookup("#photoView");
                if (photoView != null) {
                    Image img = loadImageSafe(t.getPhotoUrl());
                    if (img != null) {
                        photoView.setImage(img);
                        photoView.setVisible(true);
                        if (initialsLabel != null)
                            initialsLabel.setVisible(false);
                    } else {
                        photoView.setVisible(false);
                    }
                }

                if (emailLabel != null)
                    emailLabel.setText(t.getEmail());
                if (phoneLabel != null)
                    phoneLabel.setText(t.getPhoneNumber());
                if (addressLabel != null) {
                    addressLabel.setText(t.getDescription() != null && t.getDescription().length() > 30
                            ? t.getDescription().substring(0, 30) + "..."
                            : t.getDescription());
                }
                if (specializationLabel != null) {
                    String specText = t.getSpecialization();
                    // If sorted by proximity, show distance
                    String selectedSort = sortComboBox.getValue();
                    if (selectedSort != null && selectedSort.contains("Closest")) {
                        double dist = calculateDistance(searchLat, searchLon, t.getLatitude(), t.getLongitude());
                        if (dist < Double.MAX_VALUE) {
                            String distStr = dist < 1 ? String.format("%.0f m", dist * 1000)
                                    : String.format("%.1f km", dist);
                            specText += " • 📍 " + distStr;
                        }
                    }
                    specializationLabel.setText(specText);
                }

                if (editBtn != null)
                    editBtn.setOnAction(e -> openEditModal(t));
                if (deleteBtn != null)
                    deleteBtn.setOnAction(e -> deleteTherapist(t));

                updateCardButtonsVisibility(card, user.getRole());

                // ── Click on card body → open profile modal ──
                card.setOnMouseClicked(e -> openProfileModal(t));
                card.setStyle(card.getStyle() + "-fx-cursor: hand;");

                cardsContainer.getChildren().add(card);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ─── Profile modal: open & populate ──────────────────────────────────────
    private void openProfileModal(Therapistis t) {
        selectedProfileTherapist = t;

        // Initials
        String initials = "";
        if (t.getFirstName() != null && !t.getFirstName().isEmpty())
            initials += t.getFirstName().charAt(0);
        if (t.getLastName() != null && !t.getLastName().isEmpty())
            initials += t.getLastName().charAt(0);
        profileInitialsLabel.setText(initials.toUpperCase());

        // Show photo in modal header if URL is valid
        if (profilePhotoView != null) {
            Image img = loadImageSafe(t.getPhotoUrl());
            if (img != null) {
                profilePhotoView.setImage(img);
                profilePhotoView.setVisible(true);
                profileInitialsLabel.setVisible(false);
            } else {
                profilePhotoView.setVisible(false);
                profileInitialsLabel.setVisible(true);
            }
        }

        // Name & speciality pill
        profileNameLabel.setText("Dr. " + t.getFirstName() + " " + t.getLastName());
        profileSpecLabel.setText(t.getSpecialization() != null ? t.getSpecialization() : "—");

        // Status badge
        String status = t.getStatus();
        if ("ACTIVE".equalsIgnoreCase(status)) {
            profileStatusBadge.setText("● ACTIF");
            profileStatusBadge.setStyle(
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-background-color: #eafaf1; -fx-background-radius: 20; -fx-padding: 4 12 4 12;");
        } else {
            profileStatusBadge.setText("● INACTIF");
            profileStatusBadge.setStyle(
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #e74c3c; -fx-background-color: #fdedec; -fx-background-radius: 20; -fx-padding: 4 12 4 12;");
        }

        // Consultation type chip
        String consult = t.getConsultationType();
        String consultLabel = consult == null ? "—" : switch (consult) {
            case "ONLINE" -> "🌐 Online";
            case "IN_PERSON" -> "🏥 In person";
            case "BOTH" -> "🌐 Online & 🏥 In person";
            default -> consult;
        };
        profileConsultTypeLabel.setText(consultLabel);

        // Member since
        if (profileMemberSinceLabel != null) {
            if (t.getCreatedAt() != null) {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.setTime(t.getCreatedAt());
                int year = cal.get(java.util.Calendar.YEAR);
                profileMemberSinceLabel.setText("Member since " + year);
            } else {
                profileMemberSinceLabel.setText("");
            }
        }

        // Contact info
        profileEmailLabel.setText(t.getEmail() != null ? t.getEmail() : "—");
        profilePhoneLabel.setText(t.getPhoneNumber() != null ? t.getPhoneNumber() : "—");

        // Specialization (full)
        if (profileSpecFullLabel != null) {
            profileSpecFullLabel.setText(t.getSpecialization() != null ? t.getSpecialization() : "—");
        }

        // Description
        profileDescLabel.setText(t.getDescription() != null && !t.getDescription().isBlank()
                ? t.getDescription()
                : "No description available.");

        // Diploma button
        if (profileDiplomaBtn != null) {
            boolean hasDiploma = t.getDiplomaPath() != null && !t.getDiplomaPath().isEmpty();
            profileDiplomaBtn.setVisible(hasDiploma);
            profileDiplomaBtn.setManaged(hasDiploma);
        }

        // Map initialization
        if (mapView == null) {
            mapView = new WebView();
            mapContainer.getChildren().add(mapView);
        }
        loadMap(t.getLatitude(), t.getLongitude());

        profileModalOverlay.setVisible(true);
    }

    private void loadMap(double lat, double lon) {
        if (lat == 0 && lon == 0) {
            // Default to Tunis if no coordinates
            lat = 36.8065;
            lon = 10.1815;
        }

        String html = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n" +
                "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
                "    <style>\n" +
                "        #map { height: 100vh; width: 100vw; margin: 0; padding: 0; }\n" +
                "        body { margin: 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div id=\"map\"></div>\n" +
                "    <script>\n" +
                "        var map = L.map('map').setView([" + lat + ", " + lon + "], 13);\n" +
                "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                "            attribution: '&copy; OpenStreetMap contributors'\n" +
                "        }).addTo(map);\n" +
                "        L.marker([" + lat + ", " + lon + "]).addTo(map)\n" +
                "            .bindPopup('Emplacement du cabinet')\n" +
                "            .openPopup();\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>";
        mapView.getEngine().loadContent(html);
    }

    @FXML
    private void handleViewDiploma(ActionEvent event) {
        if (selectedProfileTherapist != null && selectedProfileTherapist.getDiplomaPath() != null) {
            try {
                java.io.File file = new java.io.File(selectedProfileTherapist.getDiplomaPath());
                if (file.exists()) {
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(file);
                    } else {
                        showAlert("Error", "Opening files is not supported on this system.");
                    }
                } else {
                    showAlert("Error", "Diploma file not found at the specified path.");
                }
            } catch (Exception e) {
                showAlert("Error", "Unable to open diploma: " + e.getMessage());
            }
        }
    }

    @FXML
    void handleCallAction(ActionEvent event) {
        if (selectedProfileTherapist != null) {
            String phone = selectedProfileTherapist.getPhoneNumber();
            showAlert("Contact therapist",
                    "You can contact Dr. " + selectedProfileTherapist.getLastName() +
                            " at the following number:\n\n" + (phone != null ? phone : "Not provided"));
        }
    }

    @FXML
    void closeProfileModal(ActionEvent event) {
        profileModalOverlay.setVisible(false);
        selectedProfileTherapist = null;
    }

    // ─── Navigate to appointment page ────────────────────────────────────────
    @FXML
    void goToAppointment(ActionEvent event) {
        if (selectedProfileTherapist != null) {
            Session.getInstance().setSelectedTherapistForBooking(selectedProfileTherapist);
        }
        profileModalOverlay.setVisible(false);
        SceneManager.loadPage("/com/example/psy/Appointment/AppointmentCalendar.fxml");
    }

    // ─── Reset filters ────────────────────────────────────────────────────────
    @FXML
    void resetFilters(ActionEvent event) {
        searchField.clear();
        specialityFilterBox.getSelectionModel().selectFirst();
        consultTypeFilterBox.getSelectionModel().selectFirst();
        sortComboBox.getSelectionModel().selectFirst();
        searchResultLabel.setText("");
    }

    // ─── Add / Edit modal ─────────────────────────────────────────────────────
    @FXML
    void openAddModal(ActionEvent event) {
        currentTherapist = null;
        modalTitle.setText("Add therapist");
        clearForm();
        modalOverlay.setVisible(true);
    }

    private void openEditModal(Therapistis t) {
        currentTherapist = t;
        modalTitle.setText("Edit therapist");
        firstNameField.setText(t.getFirstName());
        lastNameField.setText(t.getLastName());
        emailField.setText(t.getEmail());
        phoneField.setText(t.getPhoneNumber());
        specializationField.setValue(t.getSpecialization());
        descriptionArea.setText(t.getDescription());
        consultationTypeBox.setValue(t.getConsultationType());
        passwordField.setText(t.getPassword());
        photoUrlField.setText(t.getPhotoUrl() != null ? t.getPhotoUrl() : "");
        latitudeField.setText(String.valueOf(t.getLatitude()));
        longitudeField.setText(String.valueOf(t.getLongitude()));

        // Load diploma state
        selectedDiplomaFile = null;
        diplomaValid = (t.getDiplomaPath() != null && !t.getDiplomaPath().isEmpty());
        if (diplomaFileLabel != null) {
            if (t.getDiplomaPath() != null && !t.getDiplomaPath().isEmpty()) {
                File f = new File(t.getDiplomaPath());
                diplomaFileLabel.setText(f.getName());
                diplomaStatusLabel.setText("✔ Diploma already uploaded");
                diplomaStatusLabel.setStyle("-fx-text-fill: #2e7d32;");
            } else {
                diplomaFileLabel.setText("No file selected");
                diplomaStatusLabel.setText("");
            }
        }

        modalOverlay.setVisible(true);
    }

    @FXML
    void closeModal(ActionEvent event) {
        modalOverlay.setVisible(false);
        clearForm();
    }

    @FXML
    void handleSave(ActionEvent event) {
        if (!validateInput())
            return;
        try {
            if (currentTherapist == null) {
                Therapistis newT = new Therapistis();
                updateEntityFromForm(newT);
                service.create(newT);
            } else {
                updateEntityFromForm(currentTherapist);
                service.update(currentTherapist);
            }
            closeModal(null);
            loadTherapists();
            filterTherapists();
            loadSpecialityFilter();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Erreur lors de l'enregistrement: " + e.getMessage());
        }
    }

    private void updateEntityFromForm(Therapistis t) {
        t.setFirstName(firstNameField.getText());
        t.setLastName(lastNameField.getText());
        t.setEmail(emailField.getText());
        String newPassword = passwordField.getText();
        if (t.getPassword() == null || !newPassword.equals(t.getPassword()))
            t.setPassword(PasswordUtil.hashPassword(newPassword));
        t.setPhoneNumber(phoneField.getText());
        t.setSpecialization(specializationField.getValue());
        t.setDescription(descriptionArea.getText());
        t.setConsultationType(consultationTypeBox.getValue());
        t.setStatus("ACTIVE");
        String url = photoUrlField.getText() == null ? "" : photoUrlField.getText().trim();
        t.setPhotoUrl(url.isEmpty() ? null : url);

        try {
            t.setLatitude(Double.parseDouble(latitudeField.getText()));
            t.setLongitude(Double.parseDouble(longitudeField.getText()));
        } catch (Exception e) {
            t.setLatitude(0);
            t.setLongitude(0);
        }

        if (selectedDiplomaFile != null) {
            t.setDiplomaPath(selectedDiplomaFile.getAbsolutePath());
        }
    }

    private void deleteTherapist(Therapistis t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete confirmation");
        alert.setHeaderText("Delete therapist?");
        alert.setContentText("Are you sure you want to delete " + t.getFirstName() + " " + t.getLastName() + " ?");
        if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            try {
                service.delete(t.getId());
                loadTherapists();
                filterTherapists();
                loadSpecialityFilter();
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errorMsg = new StringBuilder();
        firstNameField.getStyleClass().remove("form-error");
        lastNameField.getStyleClass().remove("form-error");
        emailField.getStyleClass().remove("form-error");
        phoneField.getStyleClass().remove("form-error");
        specializationField.getStyleClass().remove("form-error");
        passwordField.getStyleClass().remove("form-error");

        if (firstNameField.getText() == null || firstNameField.getText().trim().isEmpty()) {
            errorMsg.append("• First name is required\n");
            firstNameField.getStyleClass().add("form-error");
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMsg.append("• Last name is required\n");
            lastNameField.getStyleClass().add("form-error");
        }
        if (emailField.getText() == null || !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMsg.append("• Invalid email\n");
            emailField.getStyleClass().add("form-error");
        }
        if (phoneField.getText() == null || !phoneField.getText().matches("^[24579]\\d{7}$")) {
            errorMsg.append("• Invalid Tunisian phone number (8 digits required)\n");
            phoneField.getStyleClass().add("form-error");
        }
        if (specializationField.getValue() == null || specializationField.getValue().trim().isEmpty()) {
            errorMsg.append("• Specialization is required\n");
            specializationField.getStyleClass().add("form-error");
        }
        if (passwordField.getText() == null || passwordField.getText().length() < 4) {
            errorMsg.append("• Password is required (min 4 characters)\n");
            passwordField.getStyleClass().add("form-error");
        }

        // Diploma check only for NEW therapists
        if (currentTherapist == null && selectedDiplomaFile == null) {
            errorMsg.append("• Please upload a diploma or certificate\n");
        } else if (selectedDiplomaFile != null && !diplomaValid) {
            errorMsg.append("• The diploma does not contain the required keywords\n");
        }

        if (errorMsg.length() == 0)
            return true;
        showAlert("Invalid fields", errorMsg.toString());
        return false;
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        specializationField.getSelectionModel().clearSelection();
        descriptionArea.clear();
        passwordField.clear();
        photoUrlField.clear();
        photoPreview.setVisible(false);
        photoPreviewInitials.setVisible(true);
        consultationTypeBox.getSelectionModel().clearSelection();
        latitudeField.setText("");
        longitudeField.setText("");
        selectedDiplomaFile = null;
        diplomaValid = false;
        if (diplomaFileLabel != null)
            diplomaFileLabel.setText("No file selected");
        if (diplomaStatusLabel != null)
            diplomaStatusLabel.setText("");
    }

    @FXML
    private void handleDiplomaUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select diploma or certificate");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Supported files (PDF, TXT)", "*.pdf", "*.txt"),
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"),
                new FileChooser.ExtensionFilter("Text files", "*.txt"));

        File file = fileChooser.showOpenDialog(modalOverlay.getScene().getWindow());

        if (file == null)
            return;

        selectedDiplomaFile = file;
        diplomaFileLabel.setText(file.getName());

        try {
            boolean valid = DiplomaValidator.validateDiploma(file);
            diplomaValid = valid;
            if (valid) {
                diplomaStatusLabel.setText("✔ Valid diploma — keywords detected");
                diplomaStatusLabel.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
            } else {
                diplomaStatusLabel.setText("✘ Invalid — no psychology keywords found");
                diplomaStatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
            }
        } catch (Exception e) {
            diplomaValid = false;
            diplomaStatusLabel.setText("✘ Erreur lors de la lecture du fichier");
            diplomaStatusLabel.setStyle("-fx-text-fill: #c62828; -fx-font-weight: bold;");
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    void handleSwitch(ActionEvent event) {
        SceneManager.loadPage("/com/example/psy/Therapist/availability_crud.fxml");
    }

    private void setButtonVisible(javafx.scene.control.Button btn, boolean visible) {
        if (btn != null) {
            btn.setVisible(visible);
            btn.setManaged(visible);
        }
    }

    public void updateVisibility(String role) {
        switch (role) {
            case "patient":
                setButtonVisible(ajoutdocteur, false);
                setButtonVisible(dispo, false);
                break;
            case "therapist":
                setButtonVisible(ajoutdocteur, false);
                break;
        }
    }

    private void updateCardButtonsVisibility(Node card, String role) {
        Button editBtn = (Button) card.lookup("#editButton");
        Button deleteBtn = (Button) card.lookup("#deleteButton");
        switch (role) {
            case "patient":
                setButtonVisible(editBtn, false);
                setButtonVisible(deleteBtn, false);
                break;
            case "therapist":
                setButtonVisible(editBtn, false);
                setButtonVisible(deleteBtn, false);
                break;
        }
    }
}

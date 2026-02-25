package Controllers.Therapists;

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
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import util.PasswordUtil;
import util.SceneManager;
import util.Session;

import java.io.IOException;
import java.net.URL;
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
    private TextField specializationField;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = Session.getInstance().getUser();
        service = new TherapistService();
        therapistList = FXCollections.observableArrayList();
        consultationTypeBox.setItems(FXCollections.observableArrayList("ONLINE", "IN_PERSON", "BOTH"));

        loadSpecialityFilter();
        consultTypeFilterBox.setItems(FXCollections.observableArrayList(
                "Tous les modes", "🌐 En ligne", "🏥 En présentiel", "🌐🏥 Les deux"));
        consultTypeFilterBox.getSelectionModel().selectFirst();
        loadTherapists();

        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterTherapists());
        specialityFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTherapists());
        consultTypeFilterBox.valueProperty().addListener((obs, oldVal, newVal) -> filterTherapists());

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
        try {
            List<String> specs = service.getDistinctSpecializations();
            ObservableList<String> items = FXCollections.observableArrayList();
            items.add("Toutes les spécialités");
            items.addAll(specs);
            specialityFilterBox.setItems(items);
            specialityFilterBox.getSelectionModel().selectFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
                totalTherapistsLabel.setText(list.size() + " médecin(s) enregistré(s)");
            renderCards(list);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les thérapeutes: " + e.getMessage());
        }
    }

    // ─── Client-side filter ──────────────────────────────────────────────────
    private void filterTherapists() {
        String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        String selectedSpec = specialityFilterBox.getValue();
        String selectedMode = consultTypeFilterBox.getValue();
        boolean allSpecs = selectedSpec == null || selectedSpec.equals("Toutes les spécialités");
        boolean allModes = selectedMode == null || selectedMode.equals("Tous les modes");

        List<Therapistis> filtered = therapistList.stream()
                .filter(t -> {
                    // Name search
                    boolean nameMatch = keyword.isEmpty()
                            || (t.getFirstName() != null && t.getFirstName().toLowerCase().contains(keyword))
                            || (t.getLastName() != null && t.getLastName().toLowerCase().contains(keyword))
                            || ((t.getFirstName() + " " + t.getLastName()).toLowerCase().contains(keyword));
                    // Speciality filter
                    boolean specMatch = allSpecs
                            || (t.getSpecialization() != null && t.getSpecialization().equalsIgnoreCase(selectedSpec));
                    // Consultation type filter
                    boolean modeMatch = allModes || matchConsultType(t.getConsultationType(), selectedMode);
                    return nameMatch && specMatch && modeMatch;
                })
                .collect(Collectors.toList());

        renderCards(filtered);

        boolean activeFilters = !keyword.isEmpty() || !allSpecs || !allModes;
        searchResultLabel.setText(activeFilters ? filtered.size() + " résultat(s)" : "");
    }

    private boolean matchConsultType(String therapistType, String selectedLabel) {
        if (therapistType == null)
            return false;
        return switch (selectedLabel) {
            case "🌐 En ligne" -> therapistType.equalsIgnoreCase("ONLINE");
            case "🏥 En présentiel" -> therapistType.equalsIgnoreCase("IN_PERSON");
            case "🌐🏥 Les deux" -> therapistType.equalsIgnoreCase("BOTH");
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
                    roleLabel.setText("Consultation: " + t.getConsultationType());

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
                if (specializationLabel != null)
                    specializationLabel.setText(t.getSpecialization());

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
            case "ONLINE" -> "🌐 En ligne";
            case "IN_PERSON" -> "🏥 En présentiel";
            case "BOTH" -> "🌐 En ligne & 🏥 Présentiel";
            default -> consult;
        };
        profileConsultTypeLabel.setText(consultLabel);

        // Member since
        if (t.getCreatedAt() != null) {
            String year = t.getCreatedAt().toLocalDateTime()
                    .format(DateTimeFormatter.ofPattern("MMMM yyyy", java.util.Locale.FRENCH));
            profileMemberSinceLabel.setText("Membre depuis " + year);
        } else {
            profileMemberSinceLabel.setText("");
        }

        // Contact info
        profileEmailLabel.setText(t.getEmail() != null ? t.getEmail() : "—");
        profilePhoneLabel.setText(t.getPhoneNumber() != null ? t.getPhoneNumber() : "—");

        // Specialization (full)
        profileSpecFullLabel.setText(t.getSpecialization() != null ? t.getSpecialization() : "—");

        // Description
        profileDescLabel.setText(t.getDescription() != null && !t.getDescription().isBlank()
                ? t.getDescription()
                : "Aucune description disponible.");

        profileModalOverlay.setVisible(true);
    }

    @FXML
    void closeProfileModal(ActionEvent event) {
        profileModalOverlay.setVisible(false);
        selectedProfileTherapist = null;
    }

    // ─── Navigate to appointment page ────────────────────────────────────────
    @FXML
    void goToAppointment(ActionEvent event) {
        profileModalOverlay.setVisible(false);
        SceneManager.loadPage("/com/example/psy/Appointment/AppointmentCalendar.fxml");
    }

    // ─── Reset filters ────────────────────────────────────────────────────────
    @FXML
    void resetFilters(ActionEvent event) {
        searchField.clear();
        specialityFilterBox.getSelectionModel().selectFirst();
        consultTypeFilterBox.getSelectionModel().selectFirst();
        searchResultLabel.setText("");
    }

    // ─── Add / Edit modal ─────────────────────────────────────────────────────
    @FXML
    void openAddModal(ActionEvent event) {
        currentTherapist = null;
        modalTitle.setText("Ajouter un médecin");
        clearForm();
        modalOverlay.setVisible(true);
    }

    private void openEditModal(Therapistis t) {
        currentTherapist = t;
        modalTitle.setText("Modifier le médecin");
        firstNameField.setText(t.getFirstName());
        lastNameField.setText(t.getLastName());
        emailField.setText(t.getEmail());
        phoneField.setText(t.getPhoneNumber());
        specializationField.setText(t.getSpecialization());
        descriptionArea.setText(t.getDescription());
        consultationTypeBox.setValue(t.getConsultationType());
        passwordField.setText(t.getPassword());
        photoUrlField.setText(t.getPhotoUrl() != null ? t.getPhotoUrl() : "");
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
        t.setSpecialization(specializationField.getText());
        t.setDescription(descriptionArea.getText());
        t.setConsultationType(consultationTypeBox.getValue());
        t.setStatus("ACTIVE");
        String url = photoUrlField.getText() == null ? "" : photoUrlField.getText().trim();
        t.setPhotoUrl(url.isEmpty() ? null : url);
    }

    private void deleteTherapist(Therapistis t) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation de suppression");
        alert.setHeaderText("Supprimer le thérapeute ?");
        alert.setContentText("Êtes-vous sûr de vouloir supprimer " + t.getFirstName() + " " + t.getLastName() + " ?");
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
            errorMsg.append("• Prénom requis\n");
            firstNameField.getStyleClass().add("form-error");
        }
        if (lastNameField.getText() == null || lastNameField.getText().trim().isEmpty()) {
            errorMsg.append("• Nom requis\n");
            lastNameField.getStyleClass().add("form-error");
        }
        if (emailField.getText() == null || !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errorMsg.append("• Email invalide\n");
            emailField.getStyleClass().add("form-error");
        }
        if (phoneField.getText() == null || !phoneField.getText().matches("^[24579]\\d{7}$")) {
            errorMsg.append("• Numéro tunisien invalide (8 chiffres requis)\n");
            phoneField.getStyleClass().add("form-error");
        }
        if (specializationField.getText() == null || specializationField.getText().trim().isEmpty()) {
            errorMsg.append("• Spécialisation requise\n");
            specializationField.getStyleClass().add("form-error");
        }
        if (passwordField.getText() == null || passwordField.getText().length() < 4) {
            errorMsg.append("• Mot de passe requis (min 4 caractères)\n");
            passwordField.getStyleClass().add("form-error");
        }
        if (errorMsg.length() == 0)
            return true;
        showAlert("Champs invalides", errorMsg.toString());
        return false;
    }

    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        phoneField.clear();
        specializationField.clear();
        descriptionArea.clear();
        passwordField.clear();
        photoUrlField.clear();
        photoPreview.setVisible(false);
        photoPreviewInitials.setVisible(true);
        consultationTypeBox.getSelectionModel().clearSelection();
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

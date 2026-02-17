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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import util.PasswordUtil;
import util.SceneManager;
import util.Session;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.ResourceBundle;

public class TherapistCRUDController implements Initializable {

    @FXML
    private FlowPane cardsContainer;
    @FXML
    private Label totalTherapistsLabel;

    // Modal Components
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
    private Button docteur;

    @FXML
    private Button ajoutdocteur;
    @FXML
    private Button dispo;

    private TherapistService service;
    private ObservableList<Therapistis> therapistList;
    private Therapistis currentTherapist = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User user = Session.getInstance().getUser();
        service = new TherapistService();
        therapistList = FXCollections.observableArrayList();
        consultationTypeBox.setItems(FXCollections.observableArrayList("ONLINE", "IN_PERSON", "BOTH"));

        loadTherapists();
        updateVisibility(user.getRole());
    }

    private void loadTherapists() {
        if (cardsContainer == null)
            return; // Safety check
        cardsContainer.getChildren().clear();
        try {
            List<Therapistis> list = service.list();
            therapistList.setAll(list);
            if (totalTherapistsLabel != null) {
                totalTherapistsLabel.setText(list.size() + " médecin(s) enregistré(s)");
            }

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

                    User user = Session.getInstance().getUser();

                    if (editBtn != null)
                        editBtn.setOnAction(e -> openEditModal(t));
                    if (deleteBtn != null)
                        deleteBtn.setOnAction(e -> deleteTherapist(t));

                    updateCardButtonsVisibility(card, user.getRole());

                    cardsContainer.getChildren().add(card);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger les thérapeutes: " + e.getMessage());
        }
    }

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
        // Hash the password if it's a new therapist or if the password has been
        // modified
        if (t.getPassword() == null || !newPassword.equals(t.getPassword())) {
            t.setPassword(PasswordUtil.hashPassword(newPassword));
        }

        t.setPhoneNumber(phoneField.getText());
        t.setSpecialization(specializationField.getText());
        t.setDescription(descriptionArea.getText());
        t.setConsultationType(consultationTypeBox.getValue());
        t.setStatus("ACTIVE");
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
            } catch (SQLException e) {
                e.printStackTrace();
                showAlert("Erreur", "Impossible de supprimer: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errorMsg = new StringBuilder();

        // Reset styles
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

        // Tunisian phone: exactly 8 digits, often starts with 2, 4, 5, 7, 9
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

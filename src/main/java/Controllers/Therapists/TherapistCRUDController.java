package Controllers.Therapists;

import Entities.Therapistis;
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
import util.SceneManager;

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
    private TextField userIdField;

    private TherapistService service;
    private ObservableList<Therapistis> therapistList;
    private Therapistis currentTherapist = null;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        service = new TherapistService();
        therapistList = FXCollections.observableArrayList();
        consultationTypeBox.setItems(FXCollections.observableArrayList("ONLINE", "IN_PERSON", "BOTH"));

        loadTherapists();
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

                    if (editBtn != null)
                        editBtn.setOnAction(e -> openEditModal(t));
                    if (deleteBtn != null)
                        deleteBtn.setOnAction(e -> deleteTherapist(t));

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
        userIdField.setText(String.valueOf(t.getUserId()));

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
        t.setPhoneNumber(phoneField.getText());
        t.setSpecialization(specializationField.getText());
        t.setDescription(descriptionArea.getText());
        t.setConsultationType(consultationTypeBox.getValue());
        t.setStatus("ACTIVE");
        try {
            t.setUserId(Integer.parseInt(userIdField.getText()));
        } catch (NumberFormatException e) {
            t.setUserId(0);
        }
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

        if (firstNameField.getText().isEmpty())
            errorMsg.append("Prénom requis\n");
        if (lastNameField.getText().isEmpty())
            errorMsg.append("Nom requis\n");
        if (emailField.getText().isEmpty() || !emailField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$"))
            errorMsg.append("Email invalide\n");
        if (phoneField.getText().isEmpty() || !phoneField.getText().matches("\\+?\\d{8,15}"))
            errorMsg.append("Téléphone invalide (8-15 chiffres)\n");
        if (specializationField.getText().isEmpty())
            errorMsg.append("Spécialisation requise\n");

        try {
            int uid = Integer.parseInt(userIdField.getText());
            if (uid <= 0)
                errorMsg.append("ID Utilisateur doit être positif\n");
        } catch (NumberFormatException e) {
            errorMsg.append("ID Utilisateur doit être un nombre entier\n");
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
        userIdField.clear();
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
        SceneManager.switchScene("/com/example/psy/Therapist/availability_crud.fxml");
    }
}

package Controllers.Therapists;

import Entities.Availabilities;
import Entities.Day;
import Service.AvailabilityService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import util.SceneManager;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ResourceBundle;

public class AvailabilityCRUDController implements Initializable {

    @FXML
    private TableView<Availabilities> availTable;
    @FXML
    private TableColumn<Availabilities, Integer> idColumn;
    @FXML
    private TableColumn<Availabilities, Day> dayColumn;
    @FXML
    private TableColumn<Availabilities, Time> startColumn;
    @FXML
    private TableColumn<Availabilities, Time> endColumn;
    @FXML
    private TableColumn<Availabilities, Boolean> availableColumn;
    @FXML
    private TableColumn<Availabilities, Integer> therapistIdColumn;

    @FXML
    private ComboBox<Day> dayCombo;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private CheckBox availableCheck;
    @FXML
    private TextField therapistIdField;

    private final AvailabilityService service = new AvailabilityService();
    private final ObservableList<Availabilities> data = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        therapistIdColumn.setCellValueFactory(new PropertyValueFactory<>("therapistId"));

        // Populate day ComboBox with all enum values
        dayCombo.setItems(FXCollections.observableArrayList(Day.values()));

        // Default: available
        availableCheck.setSelected(true);

        // Load all data
        loadData();

        // On row click, populate form for editing
        availTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null)
                        populateForm(newVal);
                });
    }

    private void loadData() {
        try {
            data.setAll(service.list());
            availTable.setItems(data);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement : " + e.getMessage());
        }
    }

    private void populateForm(Availabilities a) {
        dayCombo.setValue(a.getDay());
        // Display Time as HH:mm (cut off seconds)
        startTimeField.setText(a.getStartTime().toString().substring(0, 5));
        endTimeField.setText(a.getEndTime().toString().substring(0, 5));
        availableCheck.setSelected(a.isAvailable());
        therapistIdField.setText(String.valueOf(a.getTherapistId()));
    }

    // ======================== CRUD Actions ========================

    @FXML
    private void handleAdd() {
        if (!validateForm())
            return;

        Availabilities a = new Availabilities();
        fillEntityFromForm(a);

        try {
            service.create(a);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Disponibilité ajoutée avec succès.");
            loadData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de l'ajout : " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Availabilities selected = availTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une disponibilité dans le tableau.");
            return;
        }
        if (!validateForm())
            return;

        fillEntityFromForm(selected);

        try {
            service.update(selected);
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Disponibilité modifiée avec succès.");
            loadData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la modification : " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Availabilities selected = availTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Attention", "Veuillez sélectionner une disponibilité à supprimer.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText(null);
        confirm.setContentText("Voulez-vous vraiment supprimer cette disponibilité ?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Succès", "Disponibilité supprimée avec succès.");
                    loadData();
                    clearForm();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Erreur", "Échec de la suppression : " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void handleClear() {
        clearForm();
        availTable.getSelectionModel().clearSelection();
    }

    // ======================== Navigation ========================

    @FXML
    private void goToTherapists() {
        SceneManager.switchScene("/com/example/psy/Therapist/therapist_crud.fxml");
    }

    // ======================== Helpers ========================

    private void fillEntityFromForm(Availabilities a) {
        a.setDay(dayCombo.getValue());
        a.setStartTime(Time.valueOf(startTimeField.getText().trim() + ":00"));
        a.setEndTime(Time.valueOf(endTimeField.getText().trim() + ":00"));
        a.setAvailable(availableCheck.isSelected());
        a.setTherapistId(Integer.parseInt(therapistIdField.getText().trim()));
    }

    /**
     * Contrôle de saisie — validates all form fields before create/update.
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Jour — obligatoire
        if (dayCombo.getValue() == null)
            errors.append("• Le jour est obligatoire.\n");

        // Heure début — obligatoire + format HH:mm
        String startText = startTimeField.getText() != null ? startTimeField.getText().trim() : "";
        String endText = endTimeField.getText() != null ? endTimeField.getText().trim() : "";

        if (startText.isEmpty())
            errors.append("• L'heure de début est obligatoire.\n");
        else if (!startText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
            errors.append("• Format d'heure de début invalide (utilisez HH:mm).\n");

        // Heure fin — obligatoire + format HH:mm
        if (endText.isEmpty())
            errors.append("• L'heure de fin est obligatoire.\n");
        else if (!endText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$"))
            errors.append("• Format d'heure de fin invalide (utilisez HH:mm).\n");

        // Heure début < Heure fin
        if (!startText.isEmpty() && !endText.isEmpty()
                && startText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
                && endText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            Time start = Time.valueOf(startText + ":00");
            Time end = Time.valueOf(endText + ":00");
            if (!start.before(end))
                errors.append("• L'heure de début doit être avant l'heure de fin.\n");
        }

        // ID thérapeute — obligatoire + numérique + positif
        if (therapistIdField.getText() == null || therapistIdField.getText().trim().isEmpty())
            errors.append("• L'ID du thérapeute est obligatoire.\n");
        else {
            try {
                int tid = Integer.parseInt(therapistIdField.getText().trim());
                if (tid <= 0)
                    errors.append("• L'ID du thérapeute doit être positif.\n");
            } catch (NumberFormatException e) {
                errors.append("• L'ID du thérapeute doit être un nombre entier.\n");
            }
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Erreurs de validation", errors.toString());
            return false;
        }
        return true;
    }

    private void clearForm() {
        dayCombo.getSelectionModel().clearSelection();
        startTimeField.clear();
        endTimeField.clear();
        availableCheck.setSelected(true);
        therapistIdField.clear();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

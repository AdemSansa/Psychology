package Controllers.Therapists;

import Entities.Availabilities;
import Entities.Day;
import Entities.Therapistis;
import Service.AvailabilityService;
import Service.TherapistService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.scene.control.DateCell;
import javafx.scene.control.TableCell;
import javafx.util.StringConverter;
import Entities.User;
import util.SceneManager;
import util.Session;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Time;
import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.HttpURLConnection;
import java.io.InputStream;
import javafx.application.Platform;

public class AvailabilityCRUDController implements Initializable {

    @FXML
    private TableView<Availabilities> availTable;
    @FXML
    private TableColumn<Availabilities, Integer> idColumn;
    @FXML
    private TableColumn<Availabilities, java.sql.Date> dateColumn;
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
    private DatePicker datePicker;
    @FXML
    private TextField startTimeField;
    @FXML
    private TextField endTimeField;
    @FXML
    private CheckBox availableCheck;
    @FXML
    private ComboBox<Therapistis> therapistCombo;
    @FXML
    private VBox therapistBox;

    private final AvailabilityService service = new AvailabilityService();
    private final TherapistService therapistService = new TherapistService();
    private final ObservableList<Availabilities> data = FXCollections.observableArrayList();
    private Therapistis currentTherapist;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("specificDate"));

        // Colorer les dates passées en rouge
        dateColumn.setCellFactory(column -> new TableCell<Availabilities, java.sql.Date>() {
            @Override
            protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    java.time.LocalDate date = item.toLocalDate();
                    if (date.isBefore(java.time.LocalDate.now())) {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        dayColumn.setCellValueFactory(new PropertyValueFactory<>("day"));
        startColumn.setCellValueFactory(new PropertyValueFactory<>("startTime"));
        endColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
        availableColumn.setCellValueFactory(new PropertyValueFactory<>("available"));
        therapistIdColumn.setCellValueFactory(new PropertyValueFactory<>("therapistId"));
        therapistIdColumn.setCellFactory(column -> new TableCell<Availabilities, Integer>() {
            @Override
            protected void updateItem(Integer id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                } else {
                    String name = "Inconnu (" + id + ")";
                    for (Therapistis t : therapistCombo.getItems()) {
                        if (t.getId() == id) {
                            name = t.getFirstName() + " " + t.getLastName();
                            break;
                        }
                    }
                    setText(name);
                }
            }
        });

        dayCombo.setItems(FXCollections.observableArrayList(Day.values()));

        try {
            therapistCombo.setItems(FXCollections.observableArrayList(therapistService.list()));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors du chargement des thérapeutes : " + e.getMessage());
        }

        therapistCombo.setConverter(new StringConverter<Therapistis>() {
            @Override
            public String toString(Therapistis t) {
                return t == null ? null : t.getFirstName() + " " + t.getLastName();
            }

            @Override
            public Therapistis fromString(String string) {
                return null; // Not needed for selection
            }
        });

        availableCheck.setSelected(true);

        if (datePicker != null) {
            // Empêcher la sélection d'une date passée
            datePicker.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(java.time.LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (empty || date == null) {
                        setDisable(false);
                    } else {
                        setDisable(date.isBefore(java.time.LocalDate.now()));
                    }
                }
            });

            datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null) {
                    java.time.DayOfWeek dow = newVal.getDayOfWeek();
                    switch (dow) {
                        case MONDAY -> dayCombo.setValue(Day.MONDAY);
                        case TUESDAY -> dayCombo.setValue(Day.TUESDAY);
                        case WEDNESDAY -> dayCombo.setValue(Day.WEDNESDAY);
                        case THURSDAY -> dayCombo.setValue(Day.THURSDAY);
                        case FRIDAY -> dayCombo.setValue(Day.FRIDAY);
                        case SATURDAY -> dayCombo.setValue(Day.SATURDAY);
                        case SUNDAY -> dayCombo.setValue(Day.SUNDAY);
                    }
                    checkHoliday(newVal);
                }
            });
        }

        User user = Session.getInstance().getUser();
        boolean isTherapist = "therapist".equals(user.getRole());

        if (isTherapist) {
            therapistBox.setVisible(false);
            therapistBox.setManaged(false);
            therapistIdColumn.setVisible(false);
            try {
                currentTherapist = therapistService.readByEmail(user.getEmail());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        loadData();

        availTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null)
                        populateForm(newVal);
                });
    }

    private void loadData() {
        try {
            User user = Session.getInstance().getUser();
            if ("therapist".equals(user.getRole()) && currentTherapist != null) {
                data.setAll(service.listByTherapistId(currentTherapist.getId()));
            } else {
                data.setAll(service.list());
            }
            availTable.setItems(data);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur de chargement : " + e.getMessage());
        }
    }

    private void populateForm(Availabilities a) {
        if (a.getSpecificDate() != null) {
            datePicker.setValue(a.getSpecificDate().toLocalDate());
        } else {
            datePicker.setValue(null);
        }
        dayCombo.setValue(a.getDay());
        // Display Time as HH:mm (cut off seconds)
        startTimeField.setText(a.getStartTime().toString().substring(0, 5));
        endTimeField.setText(a.getEndTime().toString().substring(0, 5));
        availableCheck.setSelected(a.isAvailable());
        therapistCombo.getSelectionModel().clearSelection();
        if (a.getTherapistId() > 0) {
            for (Therapistis t : therapistCombo.getItems()) {
                if (t.getId() == a.getTherapistId()) {
                    therapistCombo.setValue(t);
                    break;
                }
            }
        }
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
        SceneManager.loadPage("/com/example/psy/Therapist/therapist_crud.fxml");
    }

    // ======================== Helpers ========================

    private void fillEntityFromForm(Availabilities a) {
        if (datePicker != null && datePicker.getValue() != null) {
            a.setSpecificDate(java.sql.Date.valueOf(datePicker.getValue()));
        } else {
            a.setSpecificDate(null);
        }
        a.setDay(dayCombo.getValue());
        a.setStartTime(Time.valueOf(startTimeField.getText().trim() + ":00"));
        a.setEndTime(Time.valueOf(endTimeField.getText().trim() + ":00"));
        a.setAvailable(availableCheck.isSelected());

        User user = Session.getInstance().getUser();
        if ("therapist".equals(user.getRole())) {
            if (currentTherapist != null) {
                a.setTherapistId(currentTherapist.getId());
            }
        } else if (therapistCombo.getValue() != null) {
            a.setTherapistId(therapistCombo.getValue().getId());
        }
    }

    /**
     * Contrôle de saisie — validates all form fields before create/update.
     */
    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Reset styles
        dayCombo.getStyleClass().remove("form-error");
        startTimeField.getStyleClass().remove("form-error");
        endTimeField.getStyleClass().remove("form-error");
        therapistCombo.getStyleClass().remove("form-error");
        if (datePicker != null)
            datePicker.getStyleClass().remove("form-error");

        // Jour — obligatoire
        if (dayCombo.getValue() == null) {
            errors.append("• Le jour est obligatoire.\n");
            dayCombo.getStyleClass().add("form-error");
        }

        // Logique Disponibilité (sans date) vs Indisponibilité (avec date)
        boolean isAvail = availableCheck.isSelected();
        if (isAvail) {
            if (datePicker != null && datePicker.getValue() != null) {
                errors.append(
                        "• Pour une disponibilité régulière (Disponible = oui), ne sélectionnez pas de date précise.\n");
                datePicker.getStyleClass().add("form-error");
            }
        } else {
            if (datePicker == null || datePicker.getValue() == null) {
                errors.append(
                        "• Pour une indisponibilité exceptionnelle (Disponible = non), veuillez choisir une date précise.\n");
                if (datePicker != null)
                    datePicker.getStyleClass().add("form-error");
            }
        }

        // Vérification de la date spécifique
        if (datePicker != null && datePicker.getValue() != null) {
            java.time.LocalDate selectedDate = datePicker.getValue();
            if (selectedDate.isBefore(java.time.LocalDate.now())) {
                errors.append("• La date choisie ne peut pas être dans le passé.\n");
                datePicker.getStyleClass().add("form-error");
            }
            if (dayCombo.getValue() != null && !selectedDate.getDayOfWeek().name().equals(dayCombo.getValue().name())) {
                errors.append("• Le menu 'Jour' ne correspond pas à la date choisie.\n");
                dayCombo.getStyleClass().add("form-error");
                datePicker.getStyleClass().add("form-error");
            }
        }

        // Heure début — obligatoire + format HH:mm
        String startText = startTimeField.getText() != null ? startTimeField.getText().trim() : "";
        String endText = endTimeField.getText() != null ? endTimeField.getText().trim() : "";

        if (startText.isEmpty()) {
            errors.append("• L'heure de début est obligatoire.\n");
            startTimeField.getStyleClass().add("form-error");
        } else if (!startText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("• Format d'heure de début invalide (utilisez HH:mm).\n");
            startTimeField.getStyleClass().add("form-error");
        }

        // Heure fin — obligatoire + format HH:mm
        if (endText.isEmpty()) {
            errors.append("• L'heure de fin est obligatoire.\n");
            endTimeField.getStyleClass().add("form-error");
        } else if (!endText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("• Format d'heure de fin invalide (utilisez HH:mm).\n");
            endTimeField.getStyleClass().add("form-error");
        }

        // Heure début < Heure fin + Durée min 15 mins
        if (!startText.isEmpty() && !endText.isEmpty()
                && startText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
                && endText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            Time start = Time.valueOf(startText + ":00");
            Time end = Time.valueOf(endText + ":00");
            if (!start.before(end)) {
                errors.append("• L'heure de début doit être avant l'heure de fin.\n");
                startTimeField.getStyleClass().add("form-error");
                endTimeField.getStyleClass().add("form-error");
            } else if ((end.getTime() - start.getTime()) < (15 * 60 * 1000)) {
                errors.append("• La durée de disponibilité doit être d'au moins 15 minutes.\n");
                startTimeField.getStyleClass().add("form-error");
                endTimeField.getStyleClass().add("form-error");
            } else {
                // Overlap check
                Availabilities temp = new Availabilities();
                temp.setDay(dayCombo.getValue());
                temp.setStartTime(start);
                temp.setEndTime(end);

                User user = Session.getInstance().getUser();
                if ("therapist".equals(user.getRole())) {
                    if (currentTherapist != null) {
                        temp.setTherapistId(currentTherapist.getId());
                    }
                } else if (therapistCombo.getValue() != null) {
                    temp.setTherapistId(therapistCombo.getValue().getId());
                }

                // Set ID if editing to exclude self from overlap
                Availabilities selected = availTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    temp.setId(selected.getId());
                }

                try {
                    if (service.isOverlap(temp)) {
                        errors.append("• Cette plage horaire chevauche une disponibilité existante.\n");
                        startTimeField.getStyleClass().add("form-error");
                        endTimeField.getStyleClass().add("form-error");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Thérapeute — obligatoire sauf si c'est un thérapeute qui utilise son propre
        // ID
        User user = Session.getInstance().getUser();
        if (!"therapist".equals(user.getRole()) && therapistCombo.getValue() == null) {
            errors.append("• Veuillez sélectionner un thérapeute.\n");
            therapistCombo.getStyleClass().add("form-error");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Erreurs de validation", errors.toString());
            return false;
        }
        return true;
    }

    private void clearForm() {
        if (datePicker != null) {
            datePicker.setValue(null);
        }
        dayCombo.getSelectionModel().clearSelection();
        startTimeField.clear();
        endTimeField.clear();
        availableCheck.setSelected(true);
        therapistCombo.getSelectionModel().clearSelection();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void checkHoliday(java.time.LocalDate date) {
        int year = date.getYear();
        String urlStr = "https://date.nager.at/api/v3/PublicHolidays/" + year + "/TN";
        new Thread(() -> {
            try {
                URL url = new URL(urlStr);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                if (conn.getResponseCode() == 200) {
                    try (InputStream is = conn.getInputStream()) {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(is);
                        boolean isHoliday = false;
                        String holidayName = "";
                        String targetDateStr = date.toString(); // YYYY-MM-DD

                        for (JsonNode node : root) {
                            String d = node.get("date").asText();
                            if (d.equals(targetDateStr)) {
                                isHoliday = true;
                                holidayName = node.get("localName").asText();
                                break;
                            }
                        }

                        if (isHoliday) {
                            final String hName = holidayName;
                            Platform.runLater(() -> {
                                availableCheck.setSelected(false);
                                showAlert(Alert.AlertType.WARNING, "Jour férié",
                                        "⚠️ Ce jour est un jour férié : " + hName
                                                + ". Disponibilité marquée comme indisponible.");
                            });
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

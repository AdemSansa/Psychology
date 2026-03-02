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
            showAlert(Alert.AlertType.ERROR, "Error", "Error while loading therapists: " + e.getMessage());
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
            showAlert(Alert.AlertType.ERROR, "Error", "Error while loading data: " + e.getMessage());
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
            showAlert(Alert.AlertType.INFORMATION, "Success", "Availability added successfully.");
            loadData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to add availability: " + e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        Availabilities selected = availTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an availability from the table.");
            return;
        }
        if (!validateForm())
            return;

        fillEntityFromForm(selected);

        try {
            service.update(selected);
            showAlert(Alert.AlertType.INFORMATION, "Success", "Availability updated successfully.");
            loadData();
            clearForm();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to update availability: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Availabilities selected = availTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Warning", "Please select an availability to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm deletion");
        confirm.setHeaderText(null);
        confirm.setContentText("Do you really want to delete this availability?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    service.delete(selected.getId());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Availability deleted successfully.");
                    loadData();
                    clearForm();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to delete availability: " + e.getMessage());
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
     * Input validation — validates all form fields before create/update.
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

        // Day — required
        if (dayCombo.getValue() == null) {
            errors.append("• Le jour est obligatoire.\n");
            dayCombo.getStyleClass().add("form-error");
        }

        // Logic: Availability (without date) vs Unavailability (with date)
        boolean isAvail = availableCheck.isSelected();
        if (isAvail) {
            if (datePicker != null && datePicker.getValue() != null) {
                errors.append(
                        "• For a regular availability (Available = yes), do not select a specific date.\n");
                datePicker.getStyleClass().add("form-error");
            }
        } else {
            if (datePicker == null || datePicker.getValue() == null) {
                errors.append(
                        "• For an exceptional unavailability (Available = no), please choose a specific date.\n");
                if (datePicker != null)
                    datePicker.getStyleClass().add("form-error");
            }
        }

        // Specific date checks
        if (datePicker != null && datePicker.getValue() != null) {
            java.time.LocalDate selectedDate = datePicker.getValue();
            if (selectedDate.isBefore(java.time.LocalDate.now())) {
                errors.append("• The selected date cannot be in the past.\n");
                datePicker.getStyleClass().add("form-error");
            }
            if (dayCombo.getValue() != null && !selectedDate.getDayOfWeek().name().equals(dayCombo.getValue().name())) {
                errors.append("• The 'Day' menu does not match the selected date.\n");
                dayCombo.getStyleClass().add("form-error");
                datePicker.getStyleClass().add("form-error");
            }
        }

        // Start time — required + format HH:mm
        String startText = startTimeField.getText() != null ? startTimeField.getText().trim() : "";
        String endText = endTimeField.getText() != null ? endTimeField.getText().trim() : "";

        if (startText.isEmpty()) {
            errors.append("• Start time is required.\n");
            startTimeField.getStyleClass().add("form-error");
        } else if (!startText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("• Invalid start time format (use HH:mm).\n");
            startTimeField.getStyleClass().add("form-error");
        }

        // End time — required + format HH:mm
        if (endText.isEmpty()) {
            errors.append("• End time is required.\n");
            endTimeField.getStyleClass().add("form-error");
        } else if (!endText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            errors.append("• Invalid end time format (use HH:mm).\n");
            endTimeField.getStyleClass().add("form-error");
        }

        // Start time < End time + minimum duration 15 mins
        if (!startText.isEmpty() && !endText.isEmpty()
                && startText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")
                && endText.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
            Time start = Time.valueOf(startText + ":00");
            Time end = Time.valueOf(endText + ":00");
            if (!start.before(end)) {
                errors.append("• Start time must be before end time.\n");
                startTimeField.getStyleClass().add("form-error");
                endTimeField.getStyleClass().add("form-error");
            } else if ((end.getTime() - start.getTime()) < (15 * 60 * 1000)) {
                errors.append("• Availability duration must be at least 15 minutes.\n");
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
                        errors.append("• This time range overlaps an existing availability.\n");
                        startTimeField.getStyleClass().add("form-error");
                        endTimeField.getStyleClass().add("form-error");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Therapist — required unless it is the logged-in therapist using their own ID
        User user = Session.getInstance().getUser();
        if (!"therapist".equals(user.getRole()) && therapistCombo.getValue() == null) {
            errors.append("• Please select a therapist.\n");
            therapistCombo.getStyleClass().add("form-error");
        }

        if (errors.length() > 0) {
            showAlert(Alert.AlertType.WARNING, "Validation errors", errors.toString());
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
                                showAlert(Alert.AlertType.WARNING, "Public holiday",
                                        "⚠️ This day is a public holiday: " + hName
                                                + ". Availability has been marked as unavailable.");
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

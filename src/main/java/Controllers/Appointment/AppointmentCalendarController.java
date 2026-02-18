package Controllers.Appointment;

import Entities.Appointment;
import Entities.Therapistis;
import Service.AppointmentService;
import Service.TherapistService;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import util.Session;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class AppointmentCalendarController {

    @FXML
    private VBox calendarContainer;
    @FXML
    private Label subtitleLabel;

    @FXML
    private HBox therapistControlsRow;
    @FXML
    private ComboBox<Therapistis> therapistComboBox;

    private CalendarView calendarView;
    private Calendar appointmentsCalendar;
    private AppointmentService appointmentService = new AppointmentService();
    private TherapistService therapistService = new TherapistService();

    private static final int APPOINTMENT_DURATION_MIN = 90;

    @FXML
    public void initialize() {
        calendarView = new CalendarView();
        appointmentsCalendar = new Calendar("Appointments");
        appointmentsCalendar.setStyle(Calendar.Style.STYLE1);

        VBox.setVgrow(calendarView, Priority.ALWAYS);
        calendarView.setMaxHeight(Double.MAX_VALUE);

        calendarView.getCalendarSources().add(
                new com.calendarfx.model.CalendarSource("My Calendars") {{
                    getCalendars().add(appointmentsCalendar);
                }}
        );

        calendarContainer.getChildren().add(calendarView);

        if (!isTherapist()) {
            loadTherapists();
            therapistComboBox.setOnAction(e -> loadAppointments());
        } else {
            therapistComboBox.setVisible(false);
            therapistComboBox.setManaged(false);
            therapistControlsRow.setVisible(false);
            therapistControlsRow.setManaged(false);
            subtitleLabel.setVisible(false);
            subtitleLabel.setManaged(false);
        }
        therapistComboBox.setButtonCell(new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(Therapistis item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toString());
                    setStyle("-fx-text-fill: white;");
                }
            }
        });
        loadAppointments();
        setupInteractions();
    }

    private boolean isTherapist() {
        return Session.getInstance().getUser() != null &&
                "therapist".equalsIgnoreCase(Session.getInstance().getUser().getRole());
    }

    private void loadTherapists() {
        therapistComboBox.getItems().clear();
        try {
            List<Therapistis> therapists = therapistService.list();
            therapistComboBox.getItems().addAll(therapists);
            if (!therapistComboBox.getItems().isEmpty()) {
                therapistComboBox.setValue(therapists.get(0));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadAppointments() {
        appointmentsCalendar.clear();
        try {
            List<Appointment> list;
            if (isTherapist()) {
                Integer therapistId = Session.getInstance().getConnectedTherapistId();
                list = appointmentService.listByTherapist(therapistId);
            } else {
                Therapistis selected = therapistComboBox.getValue();
                if (selected == null) return;
                list = appointmentService.listByTherapist(selected.getId());
            }

            for (Appointment a : list) {
                Entry<Appointment> entry = new Entry<>();
                ZonedDateTime start = a.getAppointmentDate().atTime(a.getStartTime()).atZone(ZoneId.systemDefault());
                ZonedDateTime end = a.getAppointmentDate().atTime(a.getEndTime()).atZone(ZoneId.systemDefault());

                entry.setInterval(start, end);
                entry.setUserObject(a);
                entry.setMinimumDuration(java.time.Duration.ofMinutes(APPOINTMENT_DURATION_MIN));

                LocalDate date = start.toLocalDate();
                LocalTime startTime = start.toLocalTime();
                LocalTime endTime = end.toLocalTime();

                // ✅ Set style based on status and availability
                if (!appointmentService.isWithinAvailability(a.getTherapistId(), date, startTime, endTime)) {
                    entry.getStyleClass().add("outside-hours-entry"); // dark
                } else if ("pending".equalsIgnoreCase(a.getStatus())) {
                    entry.getStyleClass().add("pending-entry"); // yellow
                } else {
                    entry.getStyleClass().add("default-entry"); // green
                }

                if (isTherapist()) {
                    entry.setTitle(appointmentService.getPatientName(a.getPatientId()));
                } else {
                    entry.setTitle("Reserved");
                }

                appointmentsCalendar.addEntry(entry);

                // Listen for interval changes
                addEntryListeners(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupInteractions() {

        // Entry creation
        calendarView.setEntryFactory(param -> {
            if (isTherapist()) return null;

            ZonedDateTime start = param.getZonedDateTime();
            ZonedDateTime end = start.plusMinutes(APPOINTMENT_DURATION_MIN);

            Therapistis therapist = therapistComboBox.getValue();
            if (therapist == null) return null;

            try {
                if (!appointmentService.isSlotAvailable(therapist.getId(),
                        start.toLocalDate(), start.toLocalTime(), end.toLocalTime(),null)) {
                    showAlert("This slot is already booked.");
                    return null;
                }
                if (!appointmentService.isWithinAvailability(therapist.getId(),
                        start.toLocalDate(), start.toLocalTime(), end.toLocalTime())) {
                    showAlert("This slot is outside therapist's working hours.");
                    return null;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return null;
            }

            Entry<Appointment> entry = new Entry<>("Pending");
            entry.setInterval(start, end);
            entry.setMinimumDuration(java.time.Duration.ofMinutes(APPOINTMENT_DURATION_MIN));
            entry.getStyleClass().add("pending-entry");

            Task<Appointment> task = new Task<>() {
                @Override
                protected Appointment call() throws Exception {
                    Appointment a = new Appointment(
                            start.toLocalDate(), start.toLocalTime(), end.toLocalTime(),
                            therapist.getId(), Session.getInstance().getUser().getId()
                    );
                    a.setStatus("pending");
                    appointmentService.create(a);
                    return a;
                }

                @Override
                protected void succeeded() {
                    entry.setUserObject(getValue());
                    appointmentsCalendar.addEntry(entry);
                    addEntryListeners(entry);
                }

                @Override
                protected void failed() {
                    getException().printStackTrace();
                }
            };
            new Thread(task).start();
            return entry;
        });

        calendarView.setEntryDetailsCallback(param -> {
            Appointment appointment = (Appointment) param.getEntry().getUserObject();
            if (appointment != null) {
                AppointmentDetailsController controller = AppointmentDetailsController.openDetails(appointment);
                controller.setCalendarController(this);
            }
            return true;
        });
    }

    private void addEntryListeners(Entry<Appointment> entry) {
        entry.intervalProperty().addListener((obs, oldInterval, newInterval) -> {
            Appointment a = entry.getUserObject();
            if (a == null) return;

            ZonedDateTime start = newInterval.getStartZonedDateTime();
            ZonedDateTime end = newInterval.getEndZonedDateTime();
            LocalDate date = start.toLocalDate();
            LocalTime startTime = start.toLocalTime();
            LocalTime endTime = end.toLocalTime();

            try {
                boolean available = appointmentService.isSlotAvailable(a.getTherapistId(), date, startTime, endTime, a.getId());
                boolean withinHours = appointmentService.isWithinAvailability(a.getTherapistId(), date, startTime, endTime);

                if (!available || !withinHours) {
                    javafx.application.Platform.runLater(() -> {
                        entry.setInterval(
                                a.getAppointmentDate().atTime(a.getStartTime()).atZone(ZoneId.systemDefault()),
                                a.getAppointmentDate().atTime(a.getEndTime()).atZone(ZoneId.systemDefault())
                        );
                        showAlert(!available ? " This slot is booked." : "Outside business hours.");
                    });
                } else {
                    a.setAppointmentDate(date);
                    a.setStartTime(startTime);
                    a.setEndTime(endTime);
                    appointmentService.update(a);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
    @FXML
    public void refreshCalendar() {
        loadAppointments();
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Invalid Appointment");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

package Controllers.Appointment;

import Entities.Appointment;
import Entities.Therapistis;
import Service.AppointmentService;
import Service.TherapistService;
import com.calendarfx.model.Calendar;
import com.calendarfx.model.CalendarEvent;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import util.Session;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.*;
import java.util.List;
import java.util.Optional;
import javafx.scene.control.ChoiceDialog;
import java.util.Timer;
import java.util.TimerTask;

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
    private Timer videoCallTimer;

    private static final int APPOINTMENT_DURATION_MIN = 90;

    @FXML
    public void initialize() {
        calendarView = new CalendarView();
        appointmentsCalendar = new Calendar("Appointments");
        appointmentsCalendar.setStyle(Calendar.Style.STYLE1);

        VBox.setVgrow(calendarView, Priority.ALWAYS);
        calendarView.setMaxHeight(Double.MAX_VALUE);

        calendarView.getCalendarSources().add(
                new com.calendarfx.model.CalendarSource("My Calendars") {
                    {
                        getCalendars().add(appointmentsCalendar);
                    }
                });

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
        startAutoVideoCallChecker();
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
                if (selected == null)
                    return;
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

                // ✅ Do NOT clear default style classes, as CalendarFX needs them for rendering!
                // entry.getStyleClass().clear();

                // ✅ Set style based on status
                if (!appointmentService.isWithinAvailability(a.getTherapistId(), date, startTime, endTime)) {
                    entry.getStyleClass().add("outside-hours-entry"); // dark gray
                } else if ("pending".equalsIgnoreCase(a.getStatus())) {
                    entry.getStyleClass().add("pending-entry"); // yellow
                } else if ("in-progress".equalsIgnoreCase(a.getStatus())) {
                    entry.getStyleClass().add("in-progress-entry"); // purple
                } else if ("confirmed".equalsIgnoreCase(a.getStatus())) {
                    entry.getStyleClass().add("confirmed-entry"); // green
                } else if ("completed".equalsIgnoreCase(a.getStatus())) {
                    entry.getStyleClass().add("completed-entry"); // blue
                } else {
                    entry.getStyleClass().add("default-entry"); // fallback
                }

                // Optional: add extra class for video call appointments
                if ("Video Call".equalsIgnoreCase(a.getType())) {
                    entry.getStyleClass().add("video-call-entry"); // you can style in CSS
                }

                // ✅ Set title based on user role
                if (isTherapist()) {
                    entry.setTitle(appointmentService.getPatientName(a.getPatientId()) + " - [" + a.getType() + "]");
                } else {
                    entry.setTitle("Reserved - [" + a.getType() + "]");
                }

                appointmentsCalendar.addEntry(entry);
                addEntryListeners(entry);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupInteractions() {

        // Entry creation
        calendarView.setEntryFactory(param -> {
            if (isTherapist()) {
                return null;
            }

            ZonedDateTime start = param.getZonedDateTime();
            ZonedDateTime end = start.plusMinutes(APPOINTMENT_DURATION_MIN);

            Therapistis therapist = therapistComboBox.getValue();
            if (therapist == null)
                return null;

            try {
                LocalDate today = LocalDate.now();
                LocalTime now = LocalTime.now();

                if (start.toLocalDate().isBefore(today) ||
                        (start.toLocalDate().equals(today) && start.toLocalTime().isBefore(now))) {
                    showAlert("Cannot book appointments in the past.");
                    return null;
                }

                if (!appointmentService.isSlotAvailable(therapist.getId(),
                        start.toLocalDate(), start.toLocalTime(), end.toLocalTime(), null)) {
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

            // Prompt user for appointment type
            List<String> choices = List.of("Presential", "Video Call");
            ChoiceDialog<String> dialog = new ChoiceDialog<>("Presential", choices);
            dialog.setTitle("Appointment Type");
            dialog.setHeaderText("Choose how you want to attend this appointment");
            dialog.setContentText("Type:");

            Optional<String> result = dialog.showAndWait();
            if (result.isEmpty()) {
                return null; // User cancelled
            }
            String selectedType = result.get();

            Entry<Appointment> entry = new Entry<>("Pending - " + selectedType);
            entry.setInterval(start, end);
            entry.setMinimumDuration(java.time.Duration.ofMinutes(APPOINTMENT_DURATION_MIN));
            entry.getStyleClass().add("pending-entry");

            Task<Appointment> task = new Task<>() {
                @Override
                protected Appointment call() throws Exception {
                    Appointment a = new Appointment(
                            start.toLocalDate(), start.toLocalTime(), end.toLocalTime(),
                            therapist.getId(), Session.getInstance().getUser().getId());
                    a.setStatus("pending");
                    a.setType(selectedType);
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
            Appointment currentAppointment = entry.getUserObject();
            if (currentAppointment == null)
                return;

            ZonedDateTime start = newInterval.getStartZonedDateTime();
            ZonedDateTime end = newInterval.getEndZonedDateTime();
            LocalDate date = start.toLocalDate();
            LocalTime startTime = start.toLocalTime();
            LocalTime endTime = end.toLocalTime();

            try {
                // Pass current appointment ID to ignore itself
                boolean available = appointmentService.isSlotAvailable(
                        currentAppointment.getTherapistId(),
                        date, startTime, endTime,
                        currentAppointment.getId() // ignore self
                );
                boolean withinHours = appointmentService.isWithinAvailability(
                        currentAppointment.getTherapistId(),
                        date, startTime, endTime);

                if (!available || !withinHours) {
                    // Find and log conflicting appointments

                    // Revert the drag
                    Platform.runLater(() -> {
                        entry.setInterval(
                                currentAppointment.getAppointmentDate()
                                        .atTime(currentAppointment.getStartTime())
                                        .atZone(ZoneId.systemDefault()),
                                currentAppointment.getAppointmentDate()
                                        .atTime(currentAppointment.getEndTime())
                                        .atZone(ZoneId.systemDefault()));
                        showAlert(!available ? "This slot is already booked." : "Outside business hours.");
                    });
                } else {
                    // Everything okay, update the appointment
                    currentAppointment.setAppointmentDate(date);
                    currentAppointment.setStartTime(startTime);
                    currentAppointment.setEndTime(endTime);
                    appointmentService.update(currentAppointment);
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

    private void startAutoVideoCallChecker() {
        videoCallTimer = new Timer(true); // daemon thread
        videoCallTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    List<Appointment> list;
                    if (isTherapist()) {
                        Integer therapistId = Session.getInstance().getConnectedTherapistId();
                        list = appointmentService.listByTherapist(therapistId);
                    } else {
                        Therapistis selected = therapistComboBox.getValue();
                        if (selected == null)
                            return;
                        list = appointmentService.listByTherapist(selected.getId());
                    }

                    for (Appointment a : list) {
                        LocalDateTime now = LocalDateTime.now();
                        LocalDateTime start = a.getAppointmentDate().atTime(a.getStartTime());
                        LocalDateTime end = a.getAppointmentDate().atTime(a.getEndTime());

                        // Start call automatically if now is at start
                        if ("confirmed".equalsIgnoreCase(a.getStatus()) &&
                                "Video Call".equalsIgnoreCase(a.getType()) &&
                                now.isAfter(start.minusSeconds(1)) && now.isBefore(start.plusSeconds(59))) {

                            Platform.runLater(() -> openVideoCall(a));
                        }

                        // Close call automatically after 90 min
                        if (now.isAfter(end) && "in-progress".equalsIgnoreCase(a.getStatus())) {
                            a.setStatus("completed");
                            appointmentService.update(a);
                        }
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 30_000); // check every 30 seconds
    }

    private void openVideoCall(Appointment appointment) {
        try {
            // generate meeting link
            String meetingLink = Service.VideoCallService.generateMeetingLink(appointment.getId());

            // open in system browser
            Service.VideoCallService.openMeetingInBrowser(meetingLink);

            // mark appointment in-progress
            appointment.setStatus("in-progress");
            appointmentService.update(appointment);

            // schedule completion
            Duration duration = Duration.between(
                    LocalDateTime.now(),
                    appointment.getAppointmentDate().atTime(appointment.getEndTime()));

            new Timer(true).schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        appointment.setStatus("completed");
                        appointmentService.update(appointment);
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }, duration.toMillis());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

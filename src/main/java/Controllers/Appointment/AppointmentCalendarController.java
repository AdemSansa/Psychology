package Controllers.Appointment;

import Entities.Appointment;
import Service.AppointmentService;

import com.calendarfx.model.Calendar;
import com.calendarfx.model.Entry;
import com.calendarfx.view.CalendarView;
import javafx.fxml.FXML;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.control.ComboBox;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class AppointmentCalendarController {

    @FXML
    private VBox calendarContainer;

    @FXML
    private ComboBox<Integer> therapistComboBox;

    private CalendarView calendarView;
    private Calendar appointmentsCalendar;
    private AppointmentService service = new AppointmentService();

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

        loadTherapists();
        loadAppointments();
        setupInteractions();
    }

    private void loadTherapists() {
        // Example static data (replace with TherapistService)
        therapistComboBox.getItems().addAll(1, 2, 3);
        if (!therapistComboBox.getItems().isEmpty()) {
            therapistComboBox.setValue(therapistComboBox.getItems().get(0));
        }
    }

    private void loadAppointments() {
        appointmentsCalendar.clear();

        try {
            List<Appointment> list = service.list();
            for (Appointment a : list) {
                Entry<Appointment> entry = new Entry<>("Booked");
                entry.setUserObject(a);
                entry.setInterval(a.getAppointmentDate().atTime(a.getStartTime()),
                        a.getAppointmentDate().atTime(a.getEndTime()));
                appointmentsCalendar.addEntry(entry);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupInteractions() {

        // Click existing appointment → open details safely
        calendarView.setEntryDetailsCallback(param -> {
            Appointment appointment = (Appointment) param.getEntry().getUserObject();
            if (appointment != null) {
                AppointmentDetailsController.openDetails(appointment);
            }
            return true;
        });

        // Create new appointment (double-click / drag)
        calendarView.setEntryFactory(param -> {

            LocalDate date = param.getZonedDateTime().toLocalDate();
            LocalTime start = param.getZonedDateTime().toLocalTime();
            LocalTime end = start.plusMinutes(90);

            // 1️⃣ Create Entry immediately to satisfy CalendarFX
            Entry<Appointment> entry = new Entry<>("New Appointment");
            entry.setInterval(date.atTime(start), date.atTime(end));

            // 2️⃣ Save the appointment in a background thread
            javafx.concurrent.Task<Appointment> task = new javafx.concurrent.Task<>() {
                @Override
                protected Appointment call() throws Exception {
                    int patientId = getLoggedInUserId();               // your logged-in patient
                    int therapistId = therapistComboBox.getValue();    // selected therapist
                    Appointment a = new Appointment(date, start, end, 1, 2);
                    service.create(a);  // insert into DB
                    return a;
                }

                @Override
                protected void succeeded() {
                    Appointment a = getValue();
                    entry.setUserObject(a);           // attach the real appointment
                    appointmentsCalendar.addEntry(entry);  // update calendar
                }

                @Override
                protected void failed() {
                    getException().printStackTrace();
                }
            };

            new Thread(task).start();

            return entry; // always return entry (CalendarFX requires this)
        });
    }

    private boolean isSlotAvailable(LocalDate date, LocalTime start, LocalTime end) {
        // TODO: implement real conflict checking
        return true;
    }

    private int getLoggedInUserId() {
        // TODO: replace with your login/session system
        return 1; // example patient ID
    }

    @FXML
    private void refreshCalendar() {
        loadAppointments();
    }
}

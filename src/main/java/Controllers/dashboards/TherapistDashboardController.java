package Controllers.dashboards;

import Entities.Appointment;
import Entities.Note;
import Service.AppointmentService;
import Service.NoteService;
import Service.AuthService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import util.SceneManager;
import util.Session;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;
import java.time.format.DateTimeFormatter;

public class TherapistDashboardController {

    @FXML
    private Label totalPatientsLabel;
    @FXML
    private Label totalSessionsLabel;
    @FXML
    private Label attendanceRateLabel;
    @FXML
    private LineChart<String, Number> moodTrendChart;
    @FXML
    private PieChart attendanceChart;

    private final AppointmentService appointmentService = new AppointmentService();
    private final NoteService noteService = new NoteService();

    @FXML
    public void initialize() {
        if (Session.getInstance().getUser() == null
                || !"therapist".equalsIgnoreCase(Session.getInstance().getUser().getRole())) {
            return;
        }

        try {
            int therapistId = Session.getInstance().getConnectedTherapistId();
            loadDashboardData(therapistId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDashboardData(int therapistId) throws SQLException {
        List<Appointment> appointments = appointmentService.listByTherapist(therapistId);

        // 1. Total Patients (Unique Patient IDs)
        long totalPatients = appointments.stream()
                .map(Appointment::getPatientId)
                .distinct()
                .count();
        totalPatientsLabel.setText(String.valueOf(totalPatients));

        // 2. Total Sessions (Completed appointments)
        long completedSessions = appointments.stream()
                .filter(a -> "completed".equalsIgnoreCase(a.getStatus()))
                .count();
        totalSessionsLabel.setText(String.valueOf(completedSessions));

        // 3. Attendance Rate
        long totalProcessed = appointments.stream()
                .filter(a -> "completed".equalsIgnoreCase(a.getStatus()) || "cancelled".equalsIgnoreCase(a.getStatus()))
                .count();

        long attendancePercent = 0;
        if (totalProcessed > 0) {
            attendancePercent = (completedSessions * 100) / totalProcessed;
        }
        attendanceRateLabel.setText(attendancePercent + "%");

        // 4. Attendance Pie Chart
        long pending = appointments.stream().filter(a -> "pending".equalsIgnoreCase(a.getStatus())).count();
        long confirmed = appointments.stream().filter(a -> "confirmed".equalsIgnoreCase(a.getStatus())).count();
        long cancelled = appointments.stream().filter(a -> "cancelled".equalsIgnoreCase(a.getStatus())).count();

        attendanceChart.getData().clear();
        if (completedSessions > 0)
            attendanceChart.getData().add(new PieChart.Data("Completed", completedSessions));
        if (pending > 0)
            attendanceChart.getData().add(new PieChart.Data("Pending", pending));
        if (confirmed > 0)
            attendanceChart.getData().add(new PieChart.Data("Confirmed", confirmed));
        if (cancelled > 0)
            attendanceChart.getData().add(new PieChart.Data("Cancelled", cancelled));

        // 5. Mood Trend Line Chart
        List<Note> allNotes = noteService.list();
        List<Note> myNotes = allNotes.stream()
                .filter(n -> n.getTherapistId() == therapistId)
                .sorted((n1, n2) -> n1.getCreatedAt().compareTo(n2.getCreatedAt()))
                .collect(Collectors.toList());

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Patient Moods");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        for (Note note : myNotes) {
            // Check if getCreatedAt is not null just in case
            if (note.getCreatedAt() != null) {
                String dateStr = note.getCreatedAt().format(formatter);
                int moodValue = parseMood(note.getMood());
                if (moodValue > 0) {
                    series.getData().add(new XYChart.Data<>(dateStr, moodValue));
                }
            }
        }

        moodTrendChart.getData().clear();
        moodTrendChart.getData().add(series);
    }

    private int parseMood(String moodStr) {
        if (moodStr == null || moodStr.trim().isEmpty())
            return 0;
        String lower = moodStr.toLowerCase();
        if (lower.contains("happy") || lower.contains("good") || lower.contains("great") || lower.contains("positive")
                || lower.contains("excellent"))
            return 3;
        if (lower.contains("neutral") || lower.contains("okay") || lower.contains("fine") || lower.contains("average"))
            return 2;
        if (lower.contains("sad") || lower.contains("bad") || lower.contains("depressed") || lower.contains("anxious")
                || lower.contains("negative"))
            return 1;

        try {
            return Integer.parseInt(moodStr.trim());
        } catch (NumberFormatException e) {
            return 2; // unknown text, assume average/neutral to keep on chart
        }
    }

    @FXML
    public void handleLogout() {
        AuthService.getInstance().logout();
        SceneManager.switchScene("/com/example/psy/auth/login.fxml");
    }
}

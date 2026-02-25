package Service;

import Database.dbconnect;
import Entities.Appointment;
import Entities.User;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EmailReminderService {

    // Replace with actual email credentials
    private static final String SENDER_EMAIL = "slimenify.app@gmail.com";
    private static final String APP_PASSWORD = "your_app_password_here";
    private UserService userService = new UserService();

    public void sendRemindersForTomorrow() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Appointment> upcomingAppointments = getAppointmentsForDate(tomorrow);

        if (upcomingAppointments.isEmpty()) {
            System.out.println("No appointments scheduled for tomorrow. No reminders to send.");
            return;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SENDER_EMAIL, APP_PASSWORD);
            }
        });

        for (Appointment appt : upcomingAppointments) {
            try {
                User patient = userService.read(appt.getPatientId());
                User therapist = userService.read(appt.getTherapistId());

                if (patient != null && patient.getEmail() != null) {
                    sendEmail(session, patient, therapist, appt);
                }
            } catch (SQLException e) {
                System.err.println("Database error retrieving users for appointment " + appt.getId());
                e.printStackTrace();
            }
        }
    }

    private void sendEmail(Session session, User patient, User therapist, Appointment appt) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(SENDER_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(patient.getEmail()));
            message.setSubject("Reminder: Upcoming Appointment with " + therapist.getFullName());

            String content = "Dear " + patient.getFullName() + ",\n\n"
                    + "This is a friendly reminder that you have a " + appt.getType().toLowerCase()
                    + " appointment scheduled for tomorrow:\n\n"
                    + "Date: " + appt.getAppointmentDate() + "\n"
                    + "Time: " + appt.getStartTime() + "\n"
                    + "Therapist: " + therapist.getFullName() + "\n\n"
                    + "Please ensure you are ready at the scheduled time.\n\n"
                    + "Best regards,\nThe Slimenify Team";

            message.setText(content);
            Transport.send(message);
            System.out.println("Sent reminder email to: " + patient.getEmail());

        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + patient.getEmail());
            e.printStackTrace();
        }
    }

    // Helper methods to get data directly for the job
    private List<Appointment> getAppointmentsForDate(LocalDate date) {
        List<Appointment> appointments = new ArrayList<>();
        String query = "SELECT * FROM appointment WHERE appointment_date = ? AND status = 'scheduled'";

        try (Connection conn = dbconnect.getInstance().getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, java.sql.Date.valueOf(date));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Appointment a = new Appointment();
                a.setId(rs.getInt("id"));
                a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
                a.setStartTime(rs.getTime("start_time").toLocalTime());
                a.setEndTime(rs.getTime("end_time").toLocalTime());
                a.setStatus(rs.getString("status"));
                a.setType(rs.getString("type"));
                a.setTherapistId(rs.getInt("therapist_id"));
                a.setPatientId(rs.getInt("patient_id"));
                appointments.add(a);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return appointments;
    }

}

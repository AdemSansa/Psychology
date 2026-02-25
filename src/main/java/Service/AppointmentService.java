package Service;

import Database.dbconnect;
import Entities.Appointment;
import Entities.Day;

import java.sql.*;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService {

    private final Connection cnx;

    public AppointmentService() {
        cnx = dbconnect.getInstance().getConnection();
    }

    public void create(Appointment appointment) throws SQLException {

        LocalTime start = appointment.getStartTime();
        LocalTime end = start.plusMinutes(90);
        appointment.setEndTime(end);

        if (!isWithinAvailability(
                appointment.getTherapistId(),
                appointment.getAppointmentDate(),
                start,
                end)) {
            throw new SQLException("Slot outside therapist availability");
        }

        if (!isSlotValid(
                appointment.getTherapistId(),
                appointment.getAppointmentDate(),
                start,
                end,
                null // excludeAppointmentId null for new appointment
        )) {
            throw new SQLException("Overlap or gap violation");
        }

        String sql = """
                INSERT INTO appointment
                (appointment_date, start_time, end_time, status, type, therapist_id, patient_id)
                VALUES (?,?,?,?,?,?,?)
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
        ps.setTime(2, Time.valueOf(start));
        ps.setTime(3, Time.valueOf(end));
        ps.setString(4, appointment.getStatus());
        ps.setString(5, appointment.getType());
        ps.setInt(6, appointment.getTherapistId());
        ps.setInt(7, appointment.getPatientId());

        ps.executeUpdate();

        System.out.println(" Appointment added!");
    }

    public Appointment read(int id) throws SQLException {
        String sql = "SELECT * FROM appointment WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return map(rs);
        } else {
            return null;
        }
    }

    public void update(Appointment appointment) throws SQLException {

        String sql = """
                UPDATE appointment
                                SET appointment_date = ?,
                                    start_time = ?,
                                    end_time = ?,
                                    status = ?,
                                    therapist_id = ?,
                                    patient_id = ?
                                WHERE id = ?
                                """;

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
        ps.setTime(2, Time.valueOf(appointment.getStartTime()));
        ps.setTime(3, Time.valueOf(appointment.getEndTime()));
        ps.setString(4, appointment.getStatus());
        ps.setInt(5, appointment.getTherapistId());
        ps.setInt(6, appointment.getPatientId());
        ps.setInt(7, appointment.getId());

        ps.executeUpdate();

        System.out.println("✅ Appointment updated!");
    }

    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM appointment WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);

        ps.executeUpdate();

        System.out.println("✅ Appointment deleted!");
    }

    public List<Appointment> listByTherapist(int therapistId) throws SQLException {

        String sql = "SELECT * FROM appointment WHERE therapist_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, therapistId);

        ResultSet rs = ps.executeQuery();
        List<Appointment> list = new ArrayList<>();

        while (rs.next()) {
            list.add(map(rs));
        }

        return list;
    }

    public boolean isSlotAvailable(int therapistId, LocalDate date, LocalTime start, LocalTime end,
            Integer ignoreAppointmentId) throws SQLException {
        List<Appointment> list = listByTherapist(therapistId);
        for (Appointment a : list) {
            if (ignoreAppointmentId != null && a.getId() == ignoreAppointmentId) {
                continue; // skip the appointment being moved
            }
            if (a.getAppointmentDate().equals(date) &&
                    !(end.isBefore(a.getStartTime()) || start.isAfter(a.getEndTime()))) {
                return false; // overlap detected
            }
        }
        return true;
    }

    public boolean isWithinAvailability(int therapistId, LocalDate date,
            LocalTime start, LocalTime end) throws SQLException {

        Day day = Day.valueOf(date.getDayOfWeek().name());

        String sql = """
                SELECT * FROM availabilities
                WHERE therapist_id = ?
                AND day = ?
                AND is_available = true
                """;

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, therapistId);
        ps.setString(2, day.name());

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            LocalTime availStart = rs.getTime("start_time").toLocalTime();
            LocalTime availEnd = rs.getTime("end_time").toLocalTime();

            if (!start.isBefore(availStart) && !end.isAfter(availEnd)) {
                return true;
            }
        }

        return false;
    }

    public String getPatientName(int patientId) {

        try {
            String sql = "SELECT first_name, last_name FROM users WHERE id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, patientId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("first_name") + " " + rs.getString("last_name");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "Patient";
    }

    public boolean isSlotValid(int therapistId, LocalDate date,
            LocalTime start, LocalTime end, Integer excludeAppointmentId) throws SQLException {

        String sql = """
                SELECT * FROM appointment
                WHERE therapist_id = ?
                AND appointment_date = ?""" +
                (excludeAppointmentId != null ? " AND id != ?" : "");

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, therapistId);
        ps.setDate(2, Date.valueOf(date));

        if (excludeAppointmentId != null) {
            ps.setInt(3, excludeAppointmentId);
        }

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {

            LocalTime existingStart = rs.getTime("start_time").toLocalTime();
            LocalTime existingEnd = rs.getTime("end_time").toLocalTime();

            // 🚨 Overlap check
            boolean overlap = start.isBefore(existingEnd) && end.isAfter(existingStart);
            if (overlap)
                return false;

            // 🚨 90-minute gap rule
            long minutesBetween = Math.abs(Duration.between(existingStart, start).toMinutes());
            if (minutesBetween < 90)
                return false;
        }

        return true;
    }

    private Appointment map(ResultSet rs) throws SQLException {

        Appointment a = new Appointment();

        a.setId(rs.getInt("id"));
        a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
        a.setStartTime(rs.getTime("start_time").toLocalTime());
        a.setEndTime(rs.getTime("end_time").toLocalTime());
        a.setStatus(rs.getString("status"));
        a.setType(rs.getString("type"));
        a.setTherapistId(rs.getInt("therapist_id"));
        a.setPatientId(rs.getInt("patient_id"));

        return a;
    }

    public int createAndReturnId(Appointment appointment) throws SQLException {
        LocalTime start = appointment.getStartTime();
        LocalTime end = start.plusMinutes(90);
        appointment.setEndTime(end);

        if (!isWithinAvailability(
                appointment.getTherapistId(),
                appointment.getAppointmentDate(),
                start,
                end)) {
            throw new SQLException("Slot outside therapist availability");
        }

        if (!isSlotValid(
                appointment.getTherapistId(),
                appointment.getAppointmentDate(),
                start,
                end,
                null)) {
            throw new SQLException("Overlap or gap violation");
        }

        String sql = """
                INSERT INTO appointment
                (appointment_date, start_time, end_time, status, type, therapist_id, patient_id)
                VALUES (?,?,?,?,?,?,?)
                """;

        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
        ps.setTime(2, Time.valueOf(start));
        ps.setTime(3, Time.valueOf(end));
        ps.setString(4, appointment.getStatus());
        ps.setString(5, appointment.getType());
        ps.setInt(6, appointment.getTherapistId());
        ps.setInt(7, appointment.getPatientId());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            return rs.getInt(1);
        }

        throw new SQLException("Failed to retrieve generated ID");
    }

}

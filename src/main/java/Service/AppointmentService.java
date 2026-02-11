package Service;

import Database.dbconnect;
import Entities.Appointment;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentService implements Iservice<Appointment> {

    @Override
    public void create(Appointment appointment) throws SQLException {

        String requete = "INSERT INTO appointment (appointment_date, start_time, end_time, status, therapist_id, patient_id) VALUES (?,?,?,?,?,?)";

        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
        statement.setTime(2, Time.valueOf(appointment.getStartTime()));
        statement.setTime(3, Time.valueOf(appointment.getEndTime()));
        statement.setString(4, appointment.getStatus());
        statement.setInt(5, appointment.getTherapistId());
        statement.setInt(6, appointment.getPatientId());

        statement.executeUpdate();
        System.out.println("Appointment added successfully!");
    }

    @Override
    public List<Appointment> list() throws SQLException {

        String requete = "SELECT * FROM appointment";
        List<Appointment> appointments = new ArrayList<>();

        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            Appointment a = new Appointment();
            a.setId(rs.getInt("id"));
            a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
            a.setStartTime(rs.getTime("start_time").toLocalTime());
            a.setEndTime(rs.getTime("end_time").toLocalTime());
            a.setStatus(rs.getString("status"));
            a.setTherapistId(rs.getInt("therapist_id"));
            a.setPatientId(rs.getInt("patient_id"));

            appointments.add(a);
        }

        return appointments;
    }

    @Override
    public Appointment read(int id) throws SQLException {

        String requete = "SELECT * FROM appointment WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();

        Appointment a = null;

        if (rs.next()) {
            a = new Appointment();
            a.setId(rs.getInt("id"));
            a.setAppointmentDate(rs.getDate("appointment_date").toLocalDate());
            a.setStartTime(rs.getTime("start_time").toLocalTime());
            a.setEndTime(rs.getTime("end_time").toLocalTime());
            a.setStatus(rs.getString("status"));
            a.setTherapistId(rs.getInt("therapist_id"));
            a.setPatientId(rs.getInt("patient_id"));
        }

        return a;
    }

    @Override
    public void update(Appointment appointment) throws SQLException {

        String requete = "UPDATE appointment SET appointment_date=?, start_time=?, end_time=?, status=?, therapist_id=?, patient_id=? WHERE id=?";

        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setDate(1, Date.valueOf(appointment.getAppointmentDate()));
        statement.setTime(2, Time.valueOf(appointment.getStartTime()));
        statement.setTime(3, Time.valueOf(appointment.getEndTime()));
        statement.setString(4, appointment.getStatus());
        statement.setInt(5, appointment.getTherapistId());
        statement.setInt(6, appointment.getPatientId());
        statement.setInt(7, appointment.getId());

        statement.executeUpdate();
        System.out.println("Appointment updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {

        String requete = "DELETE FROM appointment WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setInt(1, id);
        statement.executeUpdate();

        System.out.println("Appointment deleted successfully!");
    }
}

package Service;

import Database.dbconnect;
import Entities.Registration;

import java.sql.*;
import java.util.*;

public class RegistrationService {

    private static Connection cnx = dbconnect.getInstance().getConnection();

    // ================= CREATE =================
    public void create(Registration r) throws SQLException {

        if (isEventFull(r.getEventId()))
            throw new SQLException("Event FULL");

        if (nameExists(r.getParticipantName(), r.getEventId()))
            throw new SQLException("Name already used");

        String sql = "INSERT INTO registrations (event_id, participant_name, status, qr_code) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, r.getEventId());
        ps.setString(2, r.getParticipantName());
        ps.setString(3, r.getStatus());
        ps.setString(4, r.getQrCode());

        ps.executeUpdate();
    }

    // ================= UPDATE =================
    public void update(Registration r) throws SQLException {

        String sql = "UPDATE registrations SET participant_name=?, status=? WHERE id_registration=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, r.getParticipantName());
        ps.setString(2, r.getStatus());
        ps.setInt(3, r.getIdRegistration());

        ps.executeUpdate();
    }

    // ================= DELETE =================
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM registrations WHERE id_registration=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ================= LIST BY EVENT =================
    public List<Registration> listByEvent(int eventId) throws SQLException {

        List<Registration> list = new ArrayList<>();

        String sql = "SELECT * FROM registrations WHERE event_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);

        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Registration(
                    rs.getInt("id_registration"),
                    rs.getInt("event_id"),
                    rs.getString("participant_name"),
                    rs.getString("status"),
                    rs.getTimestamp("registration_date").toLocalDateTime(),
                    rs.getString("qr_code")
            ));
        }
        return list;
    }

    // ================= COUNT =================
    public static int countByEvent(int eventId) {
        try {
            String sql = "SELECT COUNT(*) FROM registrations WHERE event_id=?";
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, eventId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ================= EVENT FULL =================
    public boolean isEventFull(int eventId) throws SQLException {

        int registered = countByEvent(eventId);

        String sql = "SELECT max_participants FROM event WHERE id_event=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();

        int max = 0;
        if (rs.next()) max = rs.getInt(1);

        return registered >= max;
    }

    // ================= UNIQUE NAME =================
    public boolean nameExists(String name, int eventId) throws SQLException {
        String sql = "SELECT id_registration FROM registrations WHERE participant_name=? AND event_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, name);
        ps.setInt(2, eventId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public Registration list() {
        return null;
    }
}

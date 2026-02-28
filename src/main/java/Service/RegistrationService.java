package Service;

import Database.dbconnect;
import Entities.Registration;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.*;

public class RegistrationService {

    private static Connection cnx = dbconnect.getInstance().getConnection();

    // ================= HELPERS =================
    private String packMetadata(Registration r) {
        // We use the qr_code field to store metadata for email, phone, and notes
        // Format: EMAIL|PHONE|NOTES|ORIGINAL_QR
        return (r.getParticipantEmail() != null ? r.getParticipantEmail() : "") + "|" +
                (r.getParticipantPhone() != null ? r.getParticipantPhone() : "") + "|" +
                (r.getParticipantNotes() != null ? r.getParticipantNotes() : "") + "|" +
                (r.getQrCode() != null ? r.getQrCode() : "");
    }

    private void unpackMetadata(Registration r, String packed) {
        if (packed == null || !packed.contains("|"))
            return;
        String[] parts = packed.split("\\|", -1);
        if (parts.length >= 4) {
            r.setParticipantEmail(parts[0].isEmpty() ? null : parts[0]);
            r.setParticipantPhone(parts[1].isEmpty() ? null : parts[1]);
            r.setParticipantNotes(parts[2].isEmpty() ? null : parts[2]);
            // The original QR code is the last part
            r.setQrCode(parts[3]);
        }
    }

    // ================= CREATE =================
    public void create(Registration r) throws SQLException {
        if (isEventFull(r.getEventId()))
            throw new SQLException("Event FULL");

        if (nameExists(r.getParticipantName(), r.getEventId()))
            throw new SQLException("Name already used in this event");

        String sql = "INSERT INTO registrations (event_id, participant_name, status, qr_code) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, r.getEventId());
        ps.setString(2, r.getParticipantName());
        ps.setString(3, r.getStatus());
        ps.setString(4, packMetadata(r));
        ps.executeUpdate();
    }

    // ================= UPDATE =================
    public void update(Registration r) throws SQLException {
        String sql = "UPDATE registrations SET participant_name=?, status=?, qr_code=? WHERE id_registration=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, r.getParticipantName());
        ps.setString(2, r.getStatus());
        ps.setString(3, packMetadata(r));
        ps.setInt(4, r.getIdRegistration());
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
            list.add(mapResultSetToRegistration(rs));
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
            if (rs.next())
                return rs.getInt(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        if (rs.next())
            max = rs.getInt(1);
        return registered >= max;
    }

    // ================= UNIQUE NAME =================
    public boolean nameExists(String name, int eventId) throws SQLException {
        String sql = "SELECT id_registration FROM registrations WHERE LOWER(TRIM(participant_name))=LOWER(TRIM(?)) AND event_id=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, name.trim());
        ps.setInt(2, eventId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public List<Registration> list() throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(mapResultSetToRegistration(rs));
        }
        return list;
    }

    public List<Registration> listByTherapist(int therapistId) throws SQLException {
        List<Registration> list = new ArrayList<>();
        String sql = "SELECT r.* FROM registrations r JOIN event e ON r.event_id = e.id_event WHERE e.organizer_id = ?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, therapistId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(mapResultSetToRegistration(rs));
        }
        return list;
    }

    // ================= HELPERS =================
    private Registration mapResultSetToRegistration(ResultSet rs) throws SQLException {
        Timestamp ts = null;
        try {
            ts = rs.getTimestamp("registration_date");
        } catch (Exception e) {
        }

        Registration reg = new Registration(
                rs.getInt("id_registration"),
                rs.getInt("event_id"),
                rs.getString("participant_name"),
                rs.getString("status"),
                (ts != null ? ts.toLocalDateTime() : null),
                null // temporary, will be set by unpack
        );
        unpackMetadata(reg, rs.getString("qr_code"));
        return reg;
    }
}

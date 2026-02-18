package Service;

import Database.dbconnect;
import Entities.Note;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteService implements Iservice<Note> {
    private final Connection cnx;

    @Override
    public void create(Note note) throws SQLException {
        String query = "INSERT INTO note (content, mood, created_at, appointment_id, therapist_id) VALUES (?,?,?,?,?)";
        PreparedStatement stmt = dbconnect.getInstance().getConnection().prepareStatement(query);
        stmt.setString(1, note.getContent());
        stmt.setString(2, note.getMood());
        stmt.setTimestamp(3, Timestamp.valueOf(note.getCreatedAt()));
        stmt.setInt(4, note.getAppointmentId());
        stmt.setInt(5, note.getTherapistId());
        stmt.executeUpdate();
    }
    public NoteService() {
        cnx = dbconnect.getInstance().getConnection();
    }
    @Override
    public List<Note> list() throws SQLException {
        List<Note> notes = new ArrayList<>();
        String query = "SELECT * FROM note";
        ResultSet rs = dbconnect.getInstance().getConnection().prepareStatement(query).executeQuery();
        while (rs.next()) {
            Note n = new Note();
            n.setId(rs.getInt("id"));
            n.setContent(rs.getString("content"));
            n.setMood(rs.getString("mood"));
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            n.setAppointmentId(rs.getInt("appointment_id"));
            n.setTherapistId(rs.getInt("therapist_id"));
            notes.add(n);
        }
        return notes;
    }

    @Override
    public Note read(int id) throws SQLException {
        String query = "SELECT * FROM note WHERE id=?";
        PreparedStatement stmt = dbconnect.getInstance().getConnection().prepareStatement(query);
        stmt.setInt(1, id);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            Note n = new Note();
            n.setId(rs.getInt("id"));
            n.setContent(rs.getString("content"));
            n.setMood(rs.getString("mood"));
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            n.setAppointmentId(rs.getInt("appointment_id"));
            n.setTherapistId(rs.getInt("therapist_id"));
            return n;
        }
        return null;
    }

    @Override
    public void update(Note note) throws SQLException {
        String query = "UPDATE note SET content=?, mood=? WHERE id=?";
        PreparedStatement stmt = dbconnect.getInstance().getConnection().prepareStatement(query);
        stmt.setString(1, note.getContent());
        stmt.setString(2, note.getMood());
        stmt.setInt(3, note.getId());
        stmt.executeUpdate();
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM note WHERE id=?";
        PreparedStatement stmt = dbconnect.getInstance().getConnection().prepareStatement(query);
        stmt.setInt(1, id);
        stmt.executeUpdate();
    }

    public List<Note> listByAppointment(int appointmentId) throws SQLException {
        List<Note> notes = new ArrayList<>();
        String query = "SELECT * FROM note WHERE appointment_id=?";
        PreparedStatement stmt = dbconnect.getInstance().getConnection().prepareStatement(query);
        stmt.setInt(1, appointmentId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            Note n = new Note();
            n.setId(rs.getInt("id"));
            n.setContent(rs.getString("content"));
            n.setMood(rs.getString("mood"));
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            n.setAppointmentId(rs.getInt("appointment_id"));
            n.setTherapistId(rs.getInt("therapist_id"));
            notes.add(n);
        }
        return notes;
    }
    public int createAndReturnId(Note note) throws SQLException {
        String sql = """
                INSERT INTO note (content, mood, created_at, appointment_id, therapist_id)
                VALUES (?,?,?,?,?)
                """;
        PreparedStatement ps = cnx.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, note.getContent());
        ps.setString(2, note.getMood());
        ps.setTimestamp(3, Timestamp.valueOf(note.getCreatedAt()));
        ps.setInt(4, note.getAppointmentId());
        ps.setInt(5, note.getTherapistId());
        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            int id = rs.getInt(1);
            note.setId(id);
            return id;
        }
        return -1;
    }
}

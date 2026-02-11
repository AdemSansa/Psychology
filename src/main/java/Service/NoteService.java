package Service;

import Database.dbconnect;
import Entities.Note;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NoteService implements Iservice<Note> {

    @Override
    public void create(Note note) throws SQLException {

        String requete = "INSERT INTO note (content, created_at, appointment_id, therapist_id) VALUES (?,?,?,?)";

        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setString(1, note.getContent());
        statement.setTimestamp(2, Timestamp.valueOf(note.getCreatedAt()));
        statement.setInt(3, note.getAppointmentId());
        statement.setInt(4, note.getTherapistId());

        statement.executeUpdate();
        System.out.println("Note added successfully!");
    }

    @Override
    public List<Note> list() throws SQLException {

        String requete = "SELECT * FROM note";
        List<Note> notes = new ArrayList<>();

        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            Note n = new Note();
            n.setId(rs.getInt("id"));
            n.setContent(rs.getString("content"));
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            n.setAppointmentId(rs.getInt("appointment_id"));
            n.setTherapistId(rs.getInt("therapist_id"));

            notes.add(n);
        }

        return notes;
    }

    @Override
    public Note read(int id) throws SQLException {

        String requete = "SELECT * FROM note WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setInt(1, id);
        ResultSet rs = statement.executeQuery();

        Note n = null;

        if (rs.next()) {
            n = new Note();
            n.setId(rs.getInt("id"));
            n.setContent(rs.getString("content"));
            n.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            n.setAppointmentId(rs.getInt("appointment_id"));
            n.setTherapistId(rs.getInt("therapist_id"));
        }

        return n;
    }

    @Override
    public void update(Note note) throws SQLException {

        String requete = "UPDATE note SET content=?, appointment_id=?, therapist_id=? WHERE id=?";

        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setString(1, note.getContent());
        statement.setInt(2, note.getAppointmentId());
        statement.setInt(3, note.getTherapistId());
        statement.setInt(4, note.getId());

        statement.executeUpdate();
        System.out.println("Note updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {

        String requete = "DELETE FROM note WHERE id = ?";
        PreparedStatement statement = dbconnect.getInstance().getConnection().prepareStatement(requete);
        statement.setInt(1, id);
        statement.executeUpdate();

        System.out.println("Note deleted successfully!");
    }
}

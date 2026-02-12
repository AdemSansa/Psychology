package Service;

import Database.dbconnect;
import Entities.Event;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements Iservice<Event> {

    private final Connection cnx;

    public EventService() {
        cnx = dbconnect.getInstance().getConnection();
    }

    // ================= CREATE =================
    @Override
    public void create(Event event) throws SQLException {

        String sql = "INSERT INTO event (title, description, type, date_start, date_end, location, max_participants, status,image_url) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getType());
        ps.setTimestamp(4, Timestamp.valueOf(event.getDateStart()));



        if (event.getDateEnd() != null)
            ps.setTimestamp(5, Timestamp.valueOf(event.getDateEnd()));
        else
            ps.setNull(5, Types.TIMESTAMP);

        ps.setString(6, event.getLocation());

        if (event.getMaxParticipants() != null)
            ps.setInt(7, event.getMaxParticipants());
        else
            ps.setNull(7, Types.INTEGER);

        ps.setString(8, event.getStatus());

        ps.setString(9, event.getImageUrl());
        ps.executeUpdate();
        System.out.println("Event added successfully!");
    }

    // ================= READ ALL =================
    @Override
    public List<Event> list() throws SQLException {

        List<Event> events = new ArrayList<>();
        String sql = "SELECT * FROM event";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Event e = new Event(
                    rs.getInt("id_event"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("type"),
                    rs.getTimestamp("date_start").toLocalDateTime(),
                    rs.getTimestamp("date_end") != null ? rs.getTimestamp("date_end").toLocalDateTime() : null,
                    rs.getString("location"),
                    rs.getObject("max_participants") != null ? rs.getInt("max_participants") : null,
                    rs.getString("status"),

                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getObject("organizer_id") != null ? rs.getInt("organizer_id") : null,
                    rs.getString("image_url")
            );

            events.add(e);
        }

        return events;
    }

    // ================= READ BY ID =================
    @Override
    public Event read(int id) throws SQLException {

        String sql = "SELECT * FROM event WHERE id_event=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Event(
                    rs.getInt("id_event"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("type"),
                    rs.getTimestamp("date_start").toLocalDateTime(),
                    rs.getTimestamp("date_end") != null ? rs.getTimestamp("date_end").toLocalDateTime() : null,
                    rs.getString("location"),
                    rs.getObject("max_participants") != null ? rs.getInt("max_participants") : null,
                    rs.getString("status"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getObject("organizer_id") != null ? rs.getInt("organizer_id") : null,
                    rs.getString("image_url")
            );
        }

        return null;
    }

    // ================= UPDATE =================
    @Override
    public void update(Event event) throws SQLException {

        String sql = "UPDATE event SET title=?, description=?, type=?, date_start=?, date_end=?, location=?, max_participants=?, status=?, organizer_id=?, image_url=? "
                + "WHERE id_event=?";

        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setString(1, event.getTitle());
        ps.setString(2, event.getDescription());
        ps.setString(3, event.getType());
        ps.setTimestamp(4, Timestamp.valueOf(event.getDateStart()));



        if (event.getDateEnd() != null)
            ps.setTimestamp(5, Timestamp.valueOf(event.getDateEnd()));
        else
            ps.setNull(5, Types.TIMESTAMP);

        ps.setString(6, event.getLocation());

        if (event.getMaxParticipants() != null)
            ps.setInt(7, event.getMaxParticipants());
        else
            ps.setNull(7, Types.INTEGER);

        ps.setString(8, event.getStatus());
        ps.setObject(9, event.getOrganizerId());
        ps.setString(10, event.getImageUrl());
        ps.setInt(11,event.getIdEvent() );
        ps.executeUpdate();
        System.out.println("Event updated successfully!");
    }

    // ================= DELETE =================
    @Override
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM event WHERE id_event=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Event deleted successfully!");
    }
}

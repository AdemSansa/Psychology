package Service;

import Database.dbconnect;
import Entities.Availabilities;
import Entities.Day;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityService implements Iservice<Availabilities> {

    @Override
    public void create(Availabilities availability) throws SQLException {
        String query = "INSERT INTO availabilities (day, start_time, end_time, is_available, therapist_id, specific_date) VALUES (?,?,?,?,?,?)";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setString(1, availability.getDay().name());
        ps.setTime(2, availability.getStartTime());
        ps.setTime(3, availability.getEndTime());
        ps.setBoolean(4, availability.isAvailable());
        ps.setInt(5, availability.getTherapistId());
        ps.setDate(6, availability.getSpecificDate());
        ps.executeUpdate();
        System.out.println("Availability added successfully!");
    }

    @Override
    public List<Availabilities> list() throws SQLException {
        String query = "SELECT * FROM availabilities";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        List<Availabilities> list = new ArrayList<>();

        while (rs.next()) {
            Availabilities a = new Availabilities();
            a.setId(rs.getInt("id"));
            a.setDay(rs.getString("day") != null ? Day.valueOf(rs.getString("day")) : null);
            a.setStartTime(rs.getTime("start_time"));
            a.setEndTime(rs.getTime("end_time"));
            a.setAvailable(rs.getBoolean("is_available"));
            a.setTherapistId(rs.getInt("therapist_id"));
            a.setSpecificDate(rs.getDate("specific_date"));
            list.add(a);
        }
        return list;
    }

    @Override
    public Availabilities read(int id) throws SQLException {
        String query = "SELECT * FROM availabilities WHERE id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Availabilities a = null;
        if (rs.next()) {
            a = new Availabilities();
            a.setId(rs.getInt("id"));
            a.setDay(rs.getString("day") != null ? Day.valueOf(rs.getString("day")) : null);
            a.setStartTime(rs.getTime("start_time"));
            a.setEndTime(rs.getTime("end_time"));
            a.setAvailable(rs.getBoolean("is_available"));
            a.setTherapistId(rs.getInt("therapist_id"));
            a.setSpecificDate(rs.getDate("specific_date"));
        }
        return a;
    }

    @Override
    public void update(Availabilities availability) throws SQLException {
        String query = "UPDATE availabilities SET day=?, start_time=?, end_time=?, is_available=?, therapist_id=?, specific_date=? WHERE id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setString(1, availability.getDay().name());
        ps.setTime(2, availability.getStartTime());
        ps.setTime(3, availability.getEndTime());
        ps.setBoolean(4, availability.isAvailable());
        ps.setInt(5, availability.getTherapistId());
        ps.setDate(6, availability.getSpecificDate());
        ps.setInt(7, availability.getId());
        ps.executeUpdate();
        System.out.println("Availability updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM availabilities WHERE id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Availability deleted successfully!");
    }

    // --- Added for Therapist Management CRUD ---
    public List<Availabilities> listByTherapistId(int therapistId) throws SQLException {
        String query = "SELECT * FROM availabilities WHERE therapist_id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, therapistId);
        ResultSet rs = ps.executeQuery();
        List<Availabilities> list = new ArrayList<>();
        while (rs.next()) {
            Availabilities a = new Availabilities();
            a.setId(rs.getInt("id"));
            a.setDay(rs.getString("day") != null ? Day.valueOf(rs.getString("day")) : null);
            a.setStartTime(rs.getTime("start_time"));
            a.setEndTime(rs.getTime("end_time"));
            a.setAvailable(rs.getBoolean("is_available"));
            a.setTherapistId(rs.getInt("therapist_id"));
            a.setSpecificDate(rs.getDate("specific_date"));
            list.add(a);
        }
        return list;
    }

    public boolean isOverlap(Availabilities a) throws SQLException {
        // Query to find any availability for the same therapist on the same day that
        // overlaps
        // (StartA < EndB) AND (EndA > StartB)
        String query = "SELECT COUNT(*) FROM availabilities WHERE therapist_id = ? AND day = ? AND id != ? " +
                "AND ((start_time < ?) AND (end_time > ?))";

        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, a.getTherapistId());
        ps.setString(2, a.getDay().name());
        ps.setInt(3, a.getId()); // Exclude current record if updating
        ps.setTime(4, a.getEndTime());
        ps.setTime(5, a.getStartTime());

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}

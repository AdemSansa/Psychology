package Service;

import Database.dbconnect;
import Entities.Therapistis;
import Entities.User;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TherapistService implements Iservice<Therapistis> {

    @Override
    public void create(Therapistis therapist) throws SQLException {
        String query = "INSERT INTO therapists (first_name, last_name, email, password, phone_number, specialization, description, consultation_type, status, photo_url, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setString(1, therapist.getFirstName());
        ps.setString(2, therapist.getLastName());
        ps.setString(3, therapist.getEmail());
        ps.setString(4, therapist.getPassword());
        ps.setString(5, therapist.getPhoneNumber());
        ps.setString(6, therapist.getSpecialization());
        ps.setString(7, therapist.getDescription());
        ps.setString(8, therapist.getConsultationType());
        ps.setString(9, therapist.getStatus());
        ps.setString(10, therapist.getPhotoUrl());
        ps.executeUpdate();
        User user = new User();
        user.setFirstName(therapist.getFirstName());
        user.setLastName(therapist.getLastName());
        user.setEmail(therapist.getEmail());
        user.setPassword(therapist.getPassword());
        user.setRole("therapist");
        UserService userService = new UserService();
        userService.create(user);

        System.out.println("Therapist added successfully!");
    }

    @Override
    public List<Therapistis> list() throws SQLException {
        String query = "SELECT * FROM therapists";
        List<Therapistis> list = new ArrayList<>();
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            Therapistis t = new Therapistis();
            t.setId(rs.getInt("id"));
            t.setFirstName(rs.getString("first_name"));
            t.setLastName(rs.getString("last_name"));
            t.setEmail(rs.getString("email"));
            t.setPassword(rs.getString("password"));
            t.setPhoneNumber(rs.getString("phone_number"));
            t.setSpecialization(rs.getString("specialization"));
            t.setDescription(rs.getString("description"));
            t.setConsultationType(rs.getString("consultation_type"));
            t.setStatus(rs.getString("status"));
            t.setPhotoUrl(safeGetString(rs, "photo_url"));
            t.setCreatedAt(rs.getTimestamp("created_at"));
            t.setUpdatedAt(rs.getTimestamp("updated_at"));
            list.add(t);
        }
        return list;
    }

    @Override
    public Therapistis read(int id) throws SQLException {
        String query = "SELECT * FROM therapists WHERE id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        Therapistis t = null;
        if (rs.next()) {
            t = new Therapistis();
            t.setId(rs.getInt("id"));
            t.setFirstName(rs.getString("first_name"));
            t.setLastName(rs.getString("last_name"));
            t.setEmail(rs.getString("email"));
            t.setPassword(rs.getString("password"));
            t.setPhoneNumber(rs.getString("phone_number"));
            t.setSpecialization(rs.getString("specialization"));
            t.setDescription(rs.getString("description"));
            t.setConsultationType(rs.getString("consultation_type"));
            t.setStatus(rs.getString("status"));
            t.setPhotoUrl(safeGetString(rs, "photo_url"));
            t.setCreatedAt(rs.getTimestamp("created_at"));
            t.setUpdatedAt(rs.getTimestamp("updated_at"));
        }
        return t;
    }

    @Override
    public void update(Therapistis therapist) throws SQLException {
        String query = "UPDATE therapists SET first_name=?, last_name=?, email=?, password=?, phone_number=?, specialization=?, description=?, consultation_type=?, status=?, photo_url=?, updated_at=NOW() WHERE id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setString(1, therapist.getFirstName());
        ps.setString(2, therapist.getLastName());
        ps.setString(3, therapist.getEmail());
        ps.setString(4, therapist.getPassword());
        ps.setString(5, therapist.getPhoneNumber());
        ps.setString(6, therapist.getSpecialization());
        ps.setString(7, therapist.getDescription());
        ps.setString(8, therapist.getConsultationType());
        ps.setString(9, therapist.getStatus());
        ps.setString(10, therapist.getPhotoUrl());
        ps.setInt(11, therapist.getId());
        ps.executeUpdate();
        System.out.println("Therapist updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {
        String query = "DELETE FROM therapists WHERE id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Therapist deleted successfully!");
    }

    public void updatePassword(String email, String hashedPassword) throws SQLException {
        String query = "UPDATE therapists SET password = ?, updated_at = NOW() WHERE email = ?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setString(1, hashedPassword);
        ps.setString(2, email);
        ps.executeUpdate();
        System.out.println("Therapist password updated successfully!");
    }

    public Therapistis readByEmail(String email) throws SQLException {
        String query = "SELECT * FROM therapists WHERE email=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        Therapistis t = null;
        if (rs.next()) {
            t = new Therapistis();
            t.setId(rs.getInt("id"));
            t.setFirstName(rs.getString("first_name"));
            t.setLastName(rs.getString("last_name"));
            t.setEmail(rs.getString("email"));
            t.setPassword(rs.getString("password"));
            t.setPhoneNumber(rs.getString("phone_number"));
            t.setSpecialization(rs.getString("specialization"));
            t.setDescription(rs.getString("description"));
            t.setConsultationType(rs.getString("consultation_type"));
            t.setStatus(rs.getString("status"));
            t.setPhotoUrl(safeGetString(rs, "photo_url"));
            t.setCreatedAt(rs.getTimestamp("created_at"));
            t.setUpdatedAt(rs.getTimestamp("updated_at"));
        }
        return t;
    }

    /**
     * Reads a column from a ResultSet without crashing if the column does not
     * exist yet (e.g. before the ALTER TABLE migration has been run).
     */
    private String safeGetString(ResultSet rs, String column) {
        try {
            return rs.getString(column);
        } catch (SQLException e) {
            return null; // column not yet present in DB
        }
    }

    public List<String> getDistinctSpecializations() throws SQLException {
        String query = "SELECT DISTINCT specialization FROM therapists WHERE specialization IS NOT NULL AND specialization != '' ORDER BY specialization";
        List<String> list = new ArrayList<>();
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(rs.getString("specialization"));
        }
        return list;
    }

}

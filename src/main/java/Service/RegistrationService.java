package Service;

import Database.dbconnect;
import Entities.Registration;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationService implements Iservice<Registration> {

    private final Connection cnx;

    public RegistrationService() {
        cnx = dbconnect.getInstance().getConnection();
    }

    // ================= CREATE =================
    @Override
    public void create(Registration r) throws SQLException {

        String sql = "INSERT INTO registrations (user_id, event_id, status, qr_code) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, r.getUserId());
        ps.setInt(2, r.getEventId());

        if (r.getStatus() != null)
            ps.setString(3, r.getStatus());
        else
            ps.setString(3, "registered");

        if (r.getQrCode() != null)
            ps.setString(4, r.getQrCode());
        else
            ps.setNull(4, Types.LONGVARCHAR);

        ps.executeUpdate();
        System.out.println("Registration created successfully!");
    }

    // ================= READ ALL =================
    @Override
    public List<Registration> list() throws SQLException {

        List<Registration> list = new ArrayList<>();
        String sql = "SELECT * FROM registrations";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Registration r = new Registration(
                    rs.getInt("id_registration"),
                    rs.getInt("user_id"),
                    rs.getInt("event_id"),
                    rs.getString("status"),
                    rs.getTimestamp("registration_date").toLocalDateTime(),
                    rs.getString("qr_code")
            );

            list.add(r);
        }

        return list;
    }

    // ================= READ BY ID =================
    @Override
    public Registration read(int id) throws SQLException {

        String sql = "SELECT * FROM registrations WHERE id_registration=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Registration(
                    rs.getInt("id_registration"),
                    rs.getInt("user_id"),
                    rs.getInt("event_id"),
                    rs.getString("status"),
                    rs.getTimestamp("registration_date").toLocalDateTime(),
                    rs.getString("qr_code")
            );
        }

        return null;
    }

    // ================= UPDATE =================
    @Override
    public void update(Registration r) throws SQLException {

        String sql = "UPDATE registrations SET user_id=?, event_id=?, status=?, qr_code=? WHERE id_registration=?";
        PreparedStatement ps = cnx.prepareStatement(sql);

        ps.setInt(1, r.getUserId());
        ps.setInt(2, r.getEventId());
        ps.setString(3, r.getStatus());

        if (r.getQrCode() != null)
            ps.setString(4, r.getQrCode());
        else
            ps.setNull(4, Types.LONGVARCHAR);

        ps.setInt(5, r.getIdRegistration());

        ps.executeUpdate();
        System.out.println("Registration updated successfully!");
    }

    // ================= DELETE =================
    @Override
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM registrations WHERE id_registration=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Registration deleted successfully!");
    }
}

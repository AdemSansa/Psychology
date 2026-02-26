package Service;

import Database.dbconnect;
import Entities.Therapistis;
import Entities.User;
import interfaces.Iservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class TherapistService implements Iservice<Therapistis> {
    private static boolean migrationsRun = false;

    @Override
    public void create(Therapistis therapist) throws SQLException {
        runMigrations();

        // 1. Create User first to get the shared ID
        User user = new User();
        user.setFirstName(therapist.getFirstName());
        user.setLastName(therapist.getLastName());
        user.setEmail(therapist.getEmail());
        user.setPassword(therapist.getPassword());
        user.setRole("therapist");

        UserService userService = new UserService();
        userService.create(user); // This now sets the ID in the user object

        int sharedId = user.getId();
        therapist.setId(sharedId);

        // 2. Insert into therapists with same ID
        String query = "INSERT INTO therapists (id, first_name, last_name, email, password, phone_number, specialization, description, consultation_type, status, photo_url, diploma_path, latitude, longitude, created_at, updated_at) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,NOW(),NOW())";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, sharedId);
        ps.setString(2, therapist.getFirstName());
        ps.setString(3, therapist.getLastName());
        ps.setString(4, therapist.getEmail());
        ps.setString(5, therapist.getPassword());
        ps.setString(6, therapist.getPhoneNumber());
        ps.setString(7, therapist.getSpecialization());
        ps.setString(8, therapist.getDescription());
        ps.setString(9, therapist.getConsultationType());
        ps.setString(10, therapist.getStatus());
        ps.setString(11, therapist.getPhotoUrl());
        ps.setString(12, therapist.getDiplomaPath());
        ps.setDouble(13, therapist.getLatitude());
        ps.setDouble(14, therapist.getLongitude());
        ps.executeUpdate();

        System.out.println("Therapist and User added successfully with shared ID: " + sharedId);
    }

    @Override
    public List<Therapistis> list() throws SQLException {
        runMigrations();
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
            t.setDiplomaPath(safeGetString(rs, "diploma_path"));
            t.setLatitude(safeGetDouble(rs, "latitude"));
            t.setLongitude(safeGetDouble(rs, "longitude"));
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
            t.setDiplomaPath(safeGetString(rs, "diploma_path"));
            t.setLatitude(safeGetDouble(rs, "latitude"));
            t.setLongitude(safeGetDouble(rs, "longitude"));
            t.setCreatedAt(rs.getTimestamp("created_at"));
            t.setUpdatedAt(rs.getTimestamp("updated_at"));
        }
        return t;
    }

    @Override
    public void update(Therapistis therapist) throws SQLException {
        // 1. Update therapists table
        String query = "UPDATE therapists SET first_name=?, last_name=?, email=?, password=?, phone_number=?, specialization=?, description=?, consultation_type=?, status=?, photo_url=?, latitude=?, longitude=?, updated_at=NOW() WHERE id=?";
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
        ps.setDouble(11, therapist.getLatitude());
        ps.setDouble(12, therapist.getLongitude());
        ps.setInt(13, therapist.getId());
        ps.executeUpdate();

        // 2. Sync with users table
        String userQuery = "UPDATE users SET first_name=?, last_name=?, email=?, password=? WHERE id=?";
        PreparedStatement psUser = dbconnect.getInstance().getConnection().prepareStatement(userQuery);
        psUser.setString(1, therapist.getFirstName());
        psUser.setString(2, therapist.getLastName());
        psUser.setString(3, therapist.getEmail());
        psUser.setString(4, therapist.getPassword());
        psUser.setInt(5, therapist.getId());
        psUser.executeUpdate();

        System.out.println("Therapist and User updated successfully!");
    }

    @Override
    public void delete(int id) throws SQLException {
        // 1. Delete from therapists
        String query = "DELETE FROM therapists WHERE id=?";
        PreparedStatement ps = dbconnect.getInstance().getConnection().prepareStatement(query);
        ps.setInt(1, id);
        ps.executeUpdate();

        // 2. Delete from users
        String userQuery = "DELETE FROM users WHERE id=?";
        PreparedStatement psUser = dbconnect.getInstance().getConnection().prepareStatement(userQuery);
        psUser.setInt(1, id);
        psUser.executeUpdate();

        System.out.println("Therapist and User deleted successfully!");
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
            t.setDiplomaPath(safeGetString(rs, "diploma_path"));
            t.setLatitude(safeGetDouble(rs, "latitude"));
            t.setLongitude(safeGetDouble(rs, "longitude"));
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

    private double safeGetDouble(ResultSet rs, String column) {
        try {
            return rs.getDouble(column);
        } catch (SQLException e) {
            return 0.0;
        }
    }

    private void runMigrations() {
        if (migrationsRun)
            return;

        ensureLocationColumnsExist();
        migrateSpecializations();

        migrationsRun = true;
    }

    private void ensureLocationColumnsExist() {
        try {
            Connection conn = dbconnect.getInstance().getConnection();

            // Using metadata to check column existence (MySQL IF NOT EXISTS is version
            // dependent)
            if (!columnExists(conn, "therapists", "latitude")) {
                try (Statement s = conn.createStatement()) {
                    s.execute("ALTER TABLE therapists ADD COLUMN latitude DOUBLE DEFAULT 0");
                }
            }
            if (!columnExists(conn, "therapists", "longitude")) {
                try (Statement s = conn.createStatement()) {
                    s.execute("ALTER TABLE therapists ADD COLUMN longitude DOUBLE DEFAULT 0");
                }
            }
        } catch (SQLException e) {
            System.err.println("Migration notice (spatial): " + e.getMessage());
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, tableName, columnName)) {
            return rs.next();
        }
    }

    private void migrateSpecializations() {
        try {
            Connection conn = dbconnect.getInstance().getConnection();
            try (Statement s = conn.createStatement()) {
                // 1. Nettoyage des variantes de "Psychologue / Général"
                s.executeUpdate(
                        "UPDATE therapists SET specialization = 'Psychologie Générale' WHERE specialization IN ('Psychologue', 'Généraliste', 'General', 'Générale', '')");

                // 2. Harmonisation de la Dépression et Anxiété
                s.executeUpdate(
                        "UPDATE therapists SET specialization = 'Dépression & Anxiété' WHERE specialization LIKE '%Depression%' OR specialization LIKE '%Anxiété%' OR specialization LIKE '%Anxiety%'");

                // 3. Harmonisation de l'Addictologie
                s.executeUpdate(
                        "UPDATE therapists SET specialization = 'Addictologie' WHERE specialization LIKE '%Addict%'");

                // 4. Harmonisation de la Thérapie de Couple
                s.executeUpdate(
                        "UPDATE therapists SET specialization = 'Thérapie de Couple' WHERE specialization LIKE '%Couple%'");

                // 5. Harmonisation de la Pédopsychologie (Enfants)
                s.executeUpdate(
                        "UPDATE therapists SET specialization = 'Pédopsychologie' WHERE specialization LIKE '%Enfant%' OR specialization LIKE '%Ado%' OR specialization LIKE '%Child%'");

                // 6. Harmonisation de la Sexologie
                s.executeUpdate(
                        "UPDATE therapists SET specialization = 'Sexologie' WHERE specialization LIKE '%Sexe%' OR specialization LIKE '%Sexology%'");

                // 7. Harmonisation de la Neuropsychologie
                s.executeUpdate(
                        "UPDATE therapists SET specialization = 'Neuropsychologie' WHERE specialization LIKE '%Neuro%'");

                System.out.println("Specialization harmonization completed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Migration notice (specialization): " + e.getMessage());
        }
    }

}

package Service;

import Database.dbconnect;
import Entities.Review;
import interfaces.Iservice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewService implements Iservice<Review> {

    private final Connection cnx;

    public ReviewService() {
        cnx = dbconnect.getInstance().getConnection();
    }

    // ================= CREATE =================
    @Override
    public void create(Review review) throws SQLException {

        String sql = "INSERT INTO review (content, id_usr) VALUES (?, ?)";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, review.getContent());
        ps.setInt(2, review.getIdUser());

        ps.executeUpdate();
        System.out.println("Review added successfully!");
    }

    // ================= READ ALL =================
    @Override
    public List<Review> list() throws SQLException {

        List<Review> reviews = new ArrayList<>();
        String sql = "SELECT * FROM review";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            Review r = new Review(
                    rs.getInt("id_review"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getInt("id_usr")
            );
            reviews.add(r);
        }

        return reviews;
    }

    // ================= READ BY ID =================
    @Override
    public Review read(int id) throws SQLException {

        String sql = "SELECT * FROM review WHERE id_review=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Review(
                    rs.getInt("id_review"),
                    rs.getString("content"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    rs.getInt("id_usr")
            );
        }
        return null;
    }

    // ================= UPDATE =================
    @Override
    public void update(Review review) throws SQLException {

        String sql = "UPDATE review SET content=?, id_usr=? WHERE id_review=?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setString(1, review.getContent());
        ps.setInt(2, review.getIdUser());
        ps.setInt(3, review.getIdReview());

        ps.executeUpdate();
        System.out.println("Review updated successfully!");
    }

    // ================= DELETE =================
    @Override
    public void delete(int id) throws SQLException {

        String sql = "DELETE FROM review WHERE id_review=?";
        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();

        System.out.println("Review deleted successfully!");
    }
}
